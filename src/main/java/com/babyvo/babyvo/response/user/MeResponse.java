package com.babyvo.babyvo.response.user;

import java.util.UUID;

public record MeResponse(
        UUID id,
        String primaryEmail
) {}