package com.babyvo.babyvo.entity.auth;

import com.babyvo.babyvo.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Email OTP login için.
 */

@Entity
@Table(
        name = "email_otps",
        indexes = {
                @Index(name = "idx_email_otps_email_created_at", columnList = "email,createdAt")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailOtpEntity extends BaseEntity {
    @Column(nullable = false, length = 255)
    private String email; // normalized lowercase

    @Column(name = "otp_hash", nullable = false, length = 255)
    private String otpHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime consumedAt;

    @Column(nullable = false)
    private Integer attemptCount = 0;

    @Column(length = 45)
    private String ip;

    @Column(length = 255)
    private String userAgent;

    public boolean isExpired(LocalDateTime now) {
        return expiresAt.isBefore(now);
    }

    public boolean isConsumed() {
        return consumedAt != null;
    }
}
