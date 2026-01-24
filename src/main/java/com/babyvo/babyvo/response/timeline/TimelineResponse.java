package com.babyvo.babyvo.response.timeline;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TimelineResponse(
        UUID babyId,
        LocalDate date,
        List<TimelineItemResponse> items
) {}