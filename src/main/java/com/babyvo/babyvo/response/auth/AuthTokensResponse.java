package com.babyvo.babyvo.response.auth;

import java.util.UUID;

public record AuthTokensResponse(
        String accessToken,
        String refreshToken,
        UserInfo user
) {
    public record UserInfo(UUID id, String primaryEmail) {}
}