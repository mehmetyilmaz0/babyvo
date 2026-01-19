package com.babyvo.babyvo.service.auth;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class OtpGenerator {

    private final SecureRandom random = new SecureRandom();

    public String generate6Digits() {
        int n = random.nextInt(1_000_000); // 0..999999
        return String.format("%06d", n);
    }
}
