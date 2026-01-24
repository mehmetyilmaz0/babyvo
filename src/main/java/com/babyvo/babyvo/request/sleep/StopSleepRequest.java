package com.babyvo.babyvo.request.sleep;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record StopSleepRequest(
        @NotNull LocalDateTime endedAt
) {}