package com.babyvo.babyvo.repository.baby;

import com.babyvo.babyvo.entity.baby.BabyParentEntity;
import com.babyvo.babyvo.entity.enums.BabyParentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BabyParentRepository extends JpaRepository<BabyParentEntity, Long> {
    Optional<BabyParentEntity> findByBabyEntity_IdAndUserEntity_Id(UUID babyId, UUID userId);
    List<BabyParentEntity> findAllByUserEntity_IdAndStatus(UUID userId, BabyParentStatus status);
    boolean existsByBabyEntity_IdAndUserEntity_IdAndStatus(UUID babyId, UUID userId, BabyParentStatus status);
}
