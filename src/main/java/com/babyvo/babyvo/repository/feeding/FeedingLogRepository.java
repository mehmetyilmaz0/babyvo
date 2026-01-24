package com.babyvo.babyvo.repository.feeding;

import com.babyvo.babyvo.entity.enums.BreastSide;
import com.babyvo.babyvo.entity.feeding.FeedingLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface FeedingLogRepository extends JpaRepository<FeedingLogEntity, UUID> {

    Page<FeedingLogEntity> findByBabyEntity_IdAndIsDeletedFalseAndLoggedAtBetweenOrderByLoggedAtDesc(
            UUID babyId,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    Optional<FeedingLogEntity> findByIdAndIsDeletedFalse(UUID id);

    @Query("""
            select coalesce(sum(f.durationSeconds), 0)
            from FeedingLogEntity f
            where f.isDeleted = false
              and f.babyEntity.id = :babyId
              and f.type = com.babyvo.babyvo.entity.enums.FeedingType.BREAST
              and f.breastSide = :side
              and f.loggedAt between :start and :end
            """)
    long sumBreastDurationSeconds(
            @Param("babyId") UUID babyId,
            @Param("side") BreastSide side,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}