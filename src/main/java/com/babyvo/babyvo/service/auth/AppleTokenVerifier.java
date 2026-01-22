package com.babyvo.babyvo.service.auth;

import com.babyvo.babyvo.common.exception.BusinessException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Component
public class AppleTokenVerifier {

    private static final String APPLE_ISS = "https://appleid.apple.com";
    private static final String JWKS_URL = "https://appleid.apple.com/auth/keys";

    private final String clientId;

    // basit cache
    private volatile JWKSet cachedJwks;
    private volatile Instant cachedUntil = Instant.EPOCH;

    private final RestClient restClient = RestClient.create();

    public AppleTokenVerifier(@Value("${babyvo.auth.apple.client-id}") String clientId) {
        this.clientId = clientId;
    }

    public VerifiedAppleToken verify(String idToken) {
        try {
            SignedJWT jwt = SignedJWT.parse(idToken);

            // header kontrolleri
            JWSHeader header = jwt.getHeader();
            if (header.getAlgorithm() == null || !JWSAlgorithm.RS256.equals(header.getAlgorithm())) {
                throw new BusinessException(HttpStatus.UNAUTHORIZED, "APPLE_TOKEN_ALG_INVALID");
            }
            String kid = header.getKeyID();
            if (kid == null || kid.isBlank()) {
                throw new BusinessException(HttpStatus.UNAUTHORIZED, "APPLE_TOKEN_KID_MISSING");
            }

            // key bul
            RSAKey rsaKey = findRsaKeyByKid(kid)
                    .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "APPLE_JWKS_KEY_NOT_FOUND"));

            // signature verify
            boolean ok = jwt.verify(new RSASSAVerifier(rsaKey.toRSAPublicKey()));
            if (!ok) {
                throw new BusinessException(HttpStatus.UNAUTHORIZED, "APPLE_TOKEN_SIGNATURE_INVALID");
            }

            var claims = jwt.getJWTClaimsSet();

            // iss, aud, exp kontrolleri
            if (!APPLE_ISS.equals(claims.getIssuer())) {
                throw new BusinessException(HttpStatus.UNAUTHORIZED, "APPLE_TOKEN_ISS_INVALID");
            }

            boolean audOk = claims.getAudience() != null && claims.getAudience().contains(clientId);
            if (!audOk) {
                throw new BusinessException(HttpStatus.UNAUTHORIZED, "APPLE_TOKEN_AUD_INVALID");
            }

            Date exp = claims.getExpirationTime();
            if (exp == null || exp.toInstant().isBefore(Instant.now())) {
                throw new BusinessException(HttpStatus.UNAUTHORIZED, "APPLE_TOKEN_EXPIRED");
            }

            String sub = claims.getSubject();
            if (sub == null || sub.isBlank()) {
                throw new BusinessException(HttpStatus.UNAUTHORIZED, "APPLE_TOKEN_SUB_MISSING");
            }

            String email = (String) claims.getClaim("email");
            Boolean emailVerified = parseBooleanClaim(claims.getClaim("email_verified"));

            return new VerifiedAppleToken(sub, email, Boolean.TRUE.equals(emailVerified));

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "APPLE_TOKEN_INVALID");
        }
    }

    private Optional<RSAKey> findRsaKeyByKid(String kid) {
        JWKSet jwks = getJwks();
        return jwks.getKeys().stream()
                .filter(k -> kid.equals(k.getKeyID()))
                .findFirst()
                .map(k -> (RSAKey) k);
    }

    private JWKSet getJwks() {
        Instant now = Instant.now();
        if (cachedJwks != null && now.isBefore(cachedUntil)) {
            return cachedJwks;
        }

        // Apple JWKS JSON’u parse etmek için Nimbus JWKSet.parse kullanacağız
        String json = restClient.get()
                .uri(JWKS_URL)
                .retrieve()
                .body(String.class);

        try {
            cachedJwks = JWKSet.parse(json);
            cachedUntil = now.plusSeconds(60 * 60); // 1 saat cache
            return cachedJwks;
        } catch (Exception e) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "APPLE_JWKS_FETCH_FAILED");
        }
    }

    private Boolean parseBooleanClaim(Object v) {
        if (v == null) return false;
        if (v instanceof Boolean b) return b;
        if (v instanceof String s) return "true".equalsIgnoreCase(s) || "1".equals(s);
        if (v instanceof Number n) return n.intValue() == 1;
        return false;
    }

    public record VerifiedAppleToken(String subject, String email, boolean emailVerified) {}
}