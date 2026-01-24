package com.babyvo.babyvo.response.sleep;

import java.time.LocalDate;
import java.util.UUID;

public record SleepSummaryResponse(
        UUID babyId,
        LocalDate date,
        long totalMinutes,
        int sessionCount
) {}