package com.babyvo.babyvo.request.feeding;

import com.babyvo.babyvo.entity.enums.BreastSide;
import com.babyvo.babyvo.entity.enums.FeedingType;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateFeedingLogRequest(
        FeedingType type,

        // BREAST
        BreastSide breastSide,
        Integer durationSeconds,

        // BOTTLE
        Integer amountMl,

        LocalDateTime loggedAt,

        @Size(max = 500) String note
) {}