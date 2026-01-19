package com.babyvo.babyvo.service.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final byte[] secretBytes;
    private final int accessTtlMin;
    private final int refreshTtlDays;

    public JwtService(
            @Value("${babyvo.jwt.secret}") String secret,
            @Value("${babyvo.jwt.access-ttl-min:15}") int accessTtlMin,
            @Value("${babyvo.jwt.refresh-ttl-days:30}") int refreshTtlDays
    ) {
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.accessTtlMin = accessTtlMin;
        this.refreshTtlDays = refreshTtlDays;
    }

    public String createAccessToken(UUID userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(accessTtlMin, ChronoUnit.MINUTES)))
                .claim("typ", "access")
                .signWith(Keys.hmacShaKeyFor(secretBytes), SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(UUID userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(refreshTtlDays, ChronoUnit.DAYS)))
                .claim("typ", "refresh")
                .signWith(Keys.hmacShaKeyFor(secretBytes), SignatureAlgorithm.HS256)
                .compact();
    }

    public UUID parseAccessTokenAndGetUserId(String token) {
        try {
            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secretBytes))
                    .build()
                    .parseClaimsJws(token);

            String typ = jws.getBody().get("typ", String.class);
            if (!"access".equals(typ)) {
                throw new IllegalArgumentException("TOKEN_TYPE_INVALID");
            }

            return UUID.fromString(jws.getBody().getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw new IllegalArgumentException("TOKEN_INVALID", e);
        }
    }

    public UUID parseRefreshTokenAndGetUserId(String token) {
        try {
            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secretBytes))
                    .build()
                    .parseClaimsJws(token);

            String typ = jws.getBody().get("typ", String.class);
            if (!"refresh".equals(typ)) {
                throw new IllegalArgumentException("TOKEN_TYPE_INVALID");
            }

            return UUID.fromString(jws.getBody().getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw new IllegalArgumentException("TOKEN_INVALID", e);
        }
    }
}