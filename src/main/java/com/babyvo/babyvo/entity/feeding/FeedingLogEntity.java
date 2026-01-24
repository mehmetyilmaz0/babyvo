package com.babyvo.babyvo.entity.feeding;

import com.babyvo.babyvo.entity.baby.BabyEntity;
import com.babyvo.babyvo.entity.common.BaseEntity;
import com.babyvo.babyvo.entity.enums.BreastSide;
import com.babyvo.babyvo.entity.enums.FeedingType;
import com.babyvo.babyvo.entity.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "feeding_logs",
        indexes = {
                @Index(name = "idx_feeding_baby_loggedat", columnList = "baby_id,logged_at"),
                @Index(name = "idx_feeding_created_by", columnList = "created_by_user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeedingLogEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "baby_id", nullable = false, foreignKey = @ForeignKey(name = "fk_feeding_baby"))
    private BabyEntity babyEntity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_feeding_created_by"))
    private UserEntity createdByUserEntity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private FeedingType type;

    // BREAST için
    @Enumerated(EnumType.STRING)
    @Column(length = 8)
    private BreastSide breastSide;

    // BREAST için (saniye cinsinden)
    private Integer durationSeconds;

    // BOTTLE için (ml)
    private Integer amountMl;

    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt;

    @Column(length = 500)
    private String note;
}