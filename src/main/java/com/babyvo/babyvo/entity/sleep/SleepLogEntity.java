package com.babyvo.babyvo.entity.sleep;

import com.babyvo.babyvo.entity.baby.BabyEntity;
import com.babyvo.babyvo.entity.common.BaseEntity;
import com.babyvo.babyvo.entity.enums.SleepPlace;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "sleep_logs",
        indexes = {
                @Index(name = "idx_sleep_logs_baby_started_at", columnList = "baby_id, startedAt"),
                @Index(name = "idx_sleep_logs_baby_ended_at", columnList = "baby_id, endedAt"),
                @Index(name = "idx_sleep_logs_deleted", columnList = "isDeleted")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SleepLogEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "baby_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sleep_log_baby"))
    private BabyEntity babyEntity;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime endedAt; // null => still sleeping

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private SleepPlace place;

    @Column(length = 500)
    private String note;
}