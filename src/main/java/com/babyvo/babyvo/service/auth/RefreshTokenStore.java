package com.babyvo.babyvo.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenStore {

    private final StringRedisTemplate redis;

    private static final String ACTIVE_PREFIX = "babyvo:rt:active:";
    private static final String USED_PREFIX  = "babyvo:rt:used:";

    private String activeKey(String jti) { return ACTIVE_PREFIX + jti; }
    private String usedKey(String jti) { return USED_PREFIX + jti; }

    /** Login/refresh sonrası yeni refresh token'ı aktif olarak kaydet */
    public void storeActive(String jti, UUID userId, Duration ttl) {
        redis.opsForValue().set(activeKey(jti), userId.toString(), ttl);
    }

    /** Token aktif mi + user match mi */
    public boolean isActiveForUser(String jti, UUID userId) {
        String val = redis.opsForValue().get(activeKey(jti));
        return userId.toString().equals(val);
    }

    /** Atomic: ilk kez kullanılıyorsa true, tekrar kullanım ise false */
    public boolean markUsedOnce(String jti, Duration ttl) {
        Boolean ok = redis.opsForValue().setIfAbsent(usedKey(jti), "1", ttl);
        return Boolean.TRUE.equals(ok);
    }

    /** Eski refresh token'ı iptal et */
    public void revoke(String jti) {
        redis.delete(activeKey(jti));
    }
}