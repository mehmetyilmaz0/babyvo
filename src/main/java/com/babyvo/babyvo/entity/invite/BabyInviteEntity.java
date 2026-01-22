package com.babyvo.babyvo.entity.invite;

import com.babyvo.babyvo.entity.user.UserEntity;
import com.babyvo.babyvo.entity.enums.BabyPermission;
import com.babyvo.babyvo.entity.enums.InviteStatus;
import com.babyvo.babyvo.entity.baby.BabyEntity;
import com.babyvo.babyvo.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Davet mekanizması (email/QR token).
 */

@Entity
@Table(
        name = "baby_invites",
        uniqueConstraints = {
                @UniqueConstraint(name="uk_invite_token_hash", columnNames={"token_hash"})
        },
        indexes = {
                @Index(name = "idx_invites_baby_status", columnList = "baby_id,status"),
                @Index(name = "idx_invites_email_status", columnList = "invited_email,status"),
                @Index(name = "idx_invites_expires_at", columnList = "expires_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BabyInviteEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "baby_id", nullable = false, foreignKey = @ForeignKey(name = "fk_invite_baby"))
    private BabyEntity babyEntity;

    @Column(name = "invited_email", length = 255)
    private String invitedEmail; // nullable for QR invites

    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private InviteStatus status = InviteStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BabyPermission permission = BabyPermission.READ_WRITE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_invite_created_by_user"))
    private UserEntity createdByUserEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accepted_by_user_id", foreignKey = @ForeignKey(name = "fk_invite_accepted_by_user"))
    private UserEntity acceptedByUserEntity;

    private LocalDateTime acceptedAt;

    public boolean isExpired(LocalDateTime now) {
        return expiresAt.isBefore(now);
    }
}
