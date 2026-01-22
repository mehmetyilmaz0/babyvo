package com.babyvo.babyvo.response.invite;

import com.babyvo.babyvo.entity.enums.BabyParentStatus;

import java.util.UUID;

public record AcceptInviteResponse(
        UUID babyId,
        BabyParentStatus status
) {}