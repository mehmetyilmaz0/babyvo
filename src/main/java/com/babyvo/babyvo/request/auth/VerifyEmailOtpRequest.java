package com.babyvo.babyvo.request.auth;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailOtpRequest(
        @NotBlank String otpRef,
        @NotBlank String otp
) {}
