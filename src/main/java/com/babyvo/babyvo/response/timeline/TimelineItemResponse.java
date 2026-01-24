package com.babyvo.babyvo.response.timeline;

import java.time.LocalDateTime;
import java.util.UUID;

public record TimelineItemResponse(
        String type,          // "FEEDING" | "DIAPER" | "SLEEP"
        UUID id,
        LocalDateTime loggedAt,
        Object data
) {}