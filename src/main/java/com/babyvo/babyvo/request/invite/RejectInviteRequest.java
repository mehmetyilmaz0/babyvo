package com.babyvo.babyvo.request.invite;

import jakarta.validation.constraints.NotBlank;

public record RejectInviteRequest(
        @NotBlank String inviteToken
) {}