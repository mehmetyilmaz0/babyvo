package com.babyvo.babyvo.request.invite;

import com.babyvo.babyvo.entity.enums.BabyPermission;

public record CreateInviteRequest(String email, BabyPermission permission) {}