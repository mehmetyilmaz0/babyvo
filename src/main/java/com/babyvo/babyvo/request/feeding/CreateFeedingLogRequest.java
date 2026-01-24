package com.babyvo.babyvo.request.feeding;

import com.babyvo.babyvo.entity.enums.BreastSide;
import com.babyvo.babyvo.entity.enums.FeedingType;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record CreateFeedingLogRequest(
        @NotNull FeedingType type,

        // BREAST
        BreastSide breastSide,
        Integer durationSeconds,

        // BOTTLE
        Integer amountMl,

        @NotNull LocalDateTime loggedAt,

        @Size(max = 500) String note
) {}