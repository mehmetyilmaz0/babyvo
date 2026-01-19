package com.babyvo.babyvo.entity.baby;

import com.babyvo.babyvo.entity.user.UserEntity;
import com.babyvo.babyvo.entity.enums.BabySex;
import com.babyvo.babyvo.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Bebek profili.
 */

@Entity
@Table(
        name = "babies",
        indexes = {
                @Index(name = "idx_babies_created_by", columnList = "created_by_user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BabyEntity extends BaseEntity {
    @Column(nullable = false, length = 80)
    private String name;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private BabySex sex = BabySex.UNKNOWN;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_baby_created_by_user"))
    private UserEntity createdByUserEntity;
}
