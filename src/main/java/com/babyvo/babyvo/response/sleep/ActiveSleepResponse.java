package com.babyvo.babyvo.response.sleep;

import com.babyvo.babyvo.entity.sleep.SleepLogEntity;

import java.time.LocalDateTime;
import java.util.UUID;

public record ActiveSleepResponse(
        UUID id,
        UUID babyId,
        LocalDateTime startedAt,
        String note
) {
    public static ActiveSleepResponse of(SleepLogEntity s) {
        return new ActiveSleepResponse(
                s.getId(),
                s.getBabyEntity().getId(),
                s.getStartedAt(),
                s.getNote()
        );
    }
}