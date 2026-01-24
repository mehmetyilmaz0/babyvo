package com.babyvo.babyvo.response.sleep;

import com.babyvo.babyvo.entity.sleep.SleepLogEntity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public record SleepLogResponse(
        UUID id,
        UUID babyId,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        Long durationMinutes,
        String note
) {
    public static SleepLogResponse of(SleepLogEntity s) {
        Long duration = (s.getEndedAt() != null)
                ? Duration.between(s.getStartedAt(), s.getEndedAt()).toMinutes()
                : null;

        return new SleepLogResponse(
                s.getId(),
                s.getBabyEntity().getId(),
                s.getStartedAt(),
                s.getEndedAt(),
                duration,
                s.getNote()
        );
    }
}