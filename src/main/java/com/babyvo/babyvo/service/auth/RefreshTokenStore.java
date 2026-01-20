package com.babyvo.babyvo.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenStore {

    private final StringRedisTemplate redis;

    private static final String ACTIVE_PREFIX = "babyvo:rt:active:";
    private static final String USED_PREFIX  = "babyvo:rt:used:";
    private static final String USER_PREFIX  = "babyvo:rt:user:"; // ✅ userId -> set(jti)

    private String activeKey(String jti) { return ACTIVE_PREFIX + jti; }
    private String usedKey(String jti) { return USED_PREFIX + jti; }
    private String userSetKey(UUID userId) { return USER_PREFIX + userId; }

    /** Login/refresh sonrası yeni refresh token'ı aktif olarak kaydet */
    public void storeActive(String jti, UUID userId, Duration ttl) {
        // active:jti -> userId
        redis.opsForValue().set(activeKey(jti), userId.toString(), ttl);

        // user:<userId> set'ine jti ekle
        redis.opsForSet().add(userSetKey(userId), jti);

        // set key TTL: en az refresh TTL kadar yaşasın (yenilendikçe uzar)
        redis.expire(userSetKey(userId), ttl);
    }

    /** Token aktif mi + user match mi */
    public boolean isActiveForUser(String jti, UUID userId) {
        String val = redis.opsForValue().get(activeKey(jti));
        return userId.toString().equals(val);
    }

    /** Atomic: ilk kez kullanılıyorsa true, tekrar kullanım ise false */
    public boolean markUsedOnce(String jti, Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) return false;
        Boolean ok = redis.opsForValue().setIfAbsent(usedKey(jti), "1", ttl);
        return Boolean.TRUE.equals(ok);
    }

    /** Tek bir refresh token'ı iptal et */
    public void revoke(UUID userId, String jti) {
        redis.delete(activeKey(jti));
        redis.opsForSet().remove(userSetKey(userId), jti);
    }

    /** (Geri uyum) userId bilinmiyorsa sadece active siler */
    public void revoke(String jti) {
        redis.delete(activeKey(jti));
    }

    /** ✅ Logout all devices: user'ın tüm aktif refresh tokenlarını iptal et */
    public void revokeAllForUser(UUID userId) {
        String setKey = userSetKey(userId);
        Set<String> jtis = redis.opsForSet().members(setKey);

        if (jtis != null && !jtis.isEmpty()) {
            for (String jti : jtis) {
                redis.delete(activeKey(jti));
            }
        }
        redis.delete(setKey);
    }
}