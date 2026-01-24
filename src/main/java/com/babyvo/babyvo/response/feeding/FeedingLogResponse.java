package com.babyvo.babyvo.response.feeding;

import com.babyvo.babyvo.entity.enums.BreastSide;
import com.babyvo.babyvo.entity.enums.FeedingType;

import java.time.LocalDateTime;
import java.util.UUID;

public record FeedingLogResponse(
        UUID id,
        UUID babyId,
        UUID createdByUserId,
        FeedingType type,
        BreastSide breastSide,
        Integer durationSeconds,
        Integer amountMl,
        LocalDateTime loggedAt,
        String note
) {}