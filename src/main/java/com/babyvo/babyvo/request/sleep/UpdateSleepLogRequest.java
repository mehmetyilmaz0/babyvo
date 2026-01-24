package com.babyvo.babyvo.request.sleep;

import com.babyvo.babyvo.entity.enums.SleepPlace;

import java.time.LocalDateTime;

public record UpdateSleepLogRequest(
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        SleepPlace place,
        String note
) {}