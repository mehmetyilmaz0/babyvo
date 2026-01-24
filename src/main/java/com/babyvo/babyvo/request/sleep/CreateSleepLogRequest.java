package com.babyvo.babyvo.request.sleep;

import com.babyvo.babyvo.entity.enums.SleepPlace;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateSleepLogRequest(
        @NotNull LocalDateTime startedAt,
        LocalDateTime endedAt,
        SleepPlace place,
        String note
) {}