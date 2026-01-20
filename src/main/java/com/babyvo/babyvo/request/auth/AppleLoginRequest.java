package com.babyvo.babyvo.request.auth;

import jakarta.validation.constraints.NotBlank;

public record AppleLoginRequest(
        @NotBlank String idToken
        // opsiyonel: nonce doğrulaması yapmak istersen ekleyebiliriz
        // String nonce
) {}