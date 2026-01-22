package com.babyvo.babyvo.request.baby;

import com.babyvo.babyvo.entity.enums.BabySex;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateBabyRequest(
        @NotBlank String name,
        @NotNull LocalDate birthDate,
        @NotNull BabySex sex
) {}