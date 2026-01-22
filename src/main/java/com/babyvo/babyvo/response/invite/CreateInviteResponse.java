package com.babyvo.babyvo.response.invite;

public record CreateInviteResponse(
        String inviteToken,
        long expiresInHours,
        boolean emailSent
) {}