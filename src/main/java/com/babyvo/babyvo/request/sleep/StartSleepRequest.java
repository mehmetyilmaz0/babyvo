package com.babyvo.babyvo.request.sleep;

import jakarta.validation.constraints.Size;

public record StartSleepRequest(
        @Size(max = 500) String note
) {}