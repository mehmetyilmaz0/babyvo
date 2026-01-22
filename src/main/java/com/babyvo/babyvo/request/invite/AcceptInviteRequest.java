package com.babyvo.babyvo.request.invite;

import jakarta.validation.constraints.NotBlank;

public record AcceptInviteRequest(
        @NotBlank String inviteToken
) {}