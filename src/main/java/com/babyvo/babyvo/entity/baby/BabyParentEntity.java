package com.babyvo.babyvo.entity.baby;

import com.babyvo.babyvo.entity.user.UserEntity;
import com.babyvo.babyvo.entity.enums.BabyParentRole;
import com.babyvo.babyvo.entity.enums.BabyParentStatus;
import com.babyvo.babyvo.entity.enums.BabyPermission;
import com.babyvo.babyvo.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Bebeğe erişimi olan ebeveynler
 */

@Entity
@Table(
        name = "baby_parents",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_baby_parent_baby_user", columnNames = {"baby_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_baby_parents_baby_id", columnList = "baby_id"),
                @Index(name = "idx_baby_parents_user_id", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BabyParentEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "baby_id", nullable = false, foreignKey = @ForeignKey(name = "fk_baby_parent_baby"))
    private BabyEntity babyEntity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_baby_parent_user"))
    private UserEntity userEntity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BabyParentRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BabyParentStatus status = BabyParentStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BabyPermission permission = BabyPermission.READ_WRITE;
}