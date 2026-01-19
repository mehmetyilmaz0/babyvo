package com.babyvo.babyvo.service.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class OtpHasher {

    private final String secret;

    public OtpHasher(@Value("${babyvo.otp.secret}") String secret) {
        this.secret = secret;
    }

    public String hash(String emailNormalized, String otp) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] out = mac.doFinal((emailNormalized + ":" + otp).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException("OTP hash error", e);
        }
    }
}