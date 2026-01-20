package com.babyvo.babyvo.service.auth;

import com.babyvo.babyvo.common.exception.BusinessException;
import com.babyvo.babyvo.entity.user.UserEntity;
import com.babyvo.babyvo.repository.user.UserRepository;
import com.babyvo.babyvo.response.auth.AuthTokensResponse;
import com.babyvo.babyvo.service.auth.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenStore refreshTokenStore;

    @Transactional(readOnly = true)
    public AuthTokensResponse refresh(String refreshToken) {
        JwtService.ParsedRefreshToken parsed;
        try {
            parsed = jwtService.parseRefreshToken(refreshToken);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_INVALID");
        }

        UUID userId = parsed.userId();
        String jti = parsed.jti();

        // Token'ın kalan TTL'ini güvenli hesapla (negatif olmasın)
        Duration ttl = safeTtl(parsed.expiresAt());
        if (ttl.isZero()) {
            // JWT parse geçmiş olsa bile exp çok yakın/ geçmiş olabilir
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_EXPIRED");
        }

        // 1) Redis'te aktif mi?
        if (!refreshTokenStore.isActiveForUser(jti, userId)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_REVOKED_OR_EXPIRED");
        }

        // 2) Reuse (tekrar kullanım) koruması: atomic
        boolean firstUse = refreshTokenStore.markUsedOnce(jti, ttl);
        if (!firstUse) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_REUSED");
        }

        // 3) Eski refresh'i iptal et (rotation)
        refreshTokenStore.revoke(jti);

        // 4) User halen geçerli mi?
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND"));

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "USER_DELETED");
        }

        // 5) Yeni tokenlar
        String newAccess = jwtService.createAccessToken(userId);

        JwtService.IssuedRefreshToken newRefresh = jwtService.issueRefreshToken(userId);
        refreshTokenStore.storeActive(newRefresh.jti(), userId, newRefresh.ttlFromNow());

        var userInfo = new AuthTokensResponse.UserInfo(user.getId(), user.getPrimaryEmail());
        return new AuthTokensResponse(newAccess, newRefresh.token(), userInfo);
    }

    private static Duration safeTtl(Instant expiresAt) {
        Instant now = Instant.now();
        if (expiresAt == null || expiresAt.isBefore(now)) return Duration.ZERO;

        Duration d = Duration.between(now, expiresAt);

        // “0” olmasın diye min 1 saniye clamp (Redis TTL 0 bazen sorun çıkarır)
        if (d.isZero() || d.isNegative()) return Duration.ZERO;

        return d;
    }

    @Transactional
    public void logout(String refreshToken) {
        JwtService.ParsedRefreshToken parsed;
        try {
            parsed = jwtService.parseRefreshToken(refreshToken);
        } catch (Exception ignored) {
            return; // idempotent
        }

        UUID userId = parsed.userId();
        String jti = parsed.jti();

        if (refreshTokenStore.isActiveForUser(jti, userId)) {
            refreshTokenStore.revoke(userId, jti); // ✅ set'ten de düşer
        }
    }

    @Transactional
    public void logoutAllDevices(UUID userId) {
        refreshTokenStore.revokeAllForUser(userId);
    }
}