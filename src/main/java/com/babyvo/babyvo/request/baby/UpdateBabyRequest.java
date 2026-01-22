package com.babyvo.babyvo.request.baby;

import com.babyvo.babyvo.entity.enums.BabySex;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Optional;

public record UpdateBabyRequest(
        Optional<String> name,
        Optional<LocalDate> birthDate,
        Optional<BabySex> sex
) {}