package com.babyvo.babyvo.response.baby;

import com.babyvo.babyvo.entity.enums.BabySex;

import java.time.LocalDate;
import java.util.UUID;

public record BabyResponse(
        UUID id,
        String name,
        LocalDate birthDate,
        BabySex sex
) {}
