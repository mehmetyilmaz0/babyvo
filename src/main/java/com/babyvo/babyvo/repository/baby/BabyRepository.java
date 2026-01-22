package com.babyvo.babyvo.repository.baby;

import com.babyvo.babyvo.entity.baby.BabyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BabyRepository extends JpaRepository<BabyEntity, UUID> {
}
