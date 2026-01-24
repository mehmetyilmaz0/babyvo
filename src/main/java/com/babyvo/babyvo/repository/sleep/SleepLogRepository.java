package com.babyvo.babyvo.repository.sleep;

import com.babyvo.babyvo.entity.sleep.SleepLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SleepLogRepository extends JpaRepository<SleepLogEntity, UUID> {
    @Query("""
        select s from SleepLogEntity s
        where s.babyEntity.id = :babyId
          and s.isDeleted = false
          and s.startedAt >= :from
          and s.startedAt < :to
        order by s.startedAt desc
    """)
    List<SleepLogEntity> findByBabyAndStartedAtRange(
            @Param("babyId") UUID babyId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
        select s from SleepLogEntity s
        where s.babyEntity.id = :babyId
          and s.isDeleted = false
          and s.endedAt is null
        order by s.startedAt desc
    """)
    Optional<SleepLogEntity> findLatestActiveSleep(@Param("babyId") UUID babyId);

}
