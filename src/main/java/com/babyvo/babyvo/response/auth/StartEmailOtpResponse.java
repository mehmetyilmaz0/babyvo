package com.babyvo.babyvo.response.auth;

public record StartEmailOtpResponse(
        String otpRef,
        int expiresInSeconds
) {}
