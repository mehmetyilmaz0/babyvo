package com.babyvo.babyvo.response.feeding;

import java.time.LocalDate;
import java.util.UUID;

public record BreastSummaryResponse(
        UUID babyId,
        LocalDate date,
        long leftDurationSeconds,
        long rightDurationSeconds,
        long totalDurationSeconds
) {}