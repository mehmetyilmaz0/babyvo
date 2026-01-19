package com.babyvo.babyvo.repository.auth;

import com.babyvo.babyvo.entity.auth.EmailOtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface EmailOtpRepository extends JpaRepository<EmailOtpEntity, UUID> {

    Optional<EmailOtpEntity> findByIdAndIsDeletedFalse(UUID id);

    long countByEmailAndCreatedAtAfterAndIsDeletedFalse(String email, LocalDateTime after);

    // aktif OTP var mı (opsiyonel)
    Optional<EmailOtpEntity> findFirstByEmailAndConsumedAtIsNullAndExpiresAtAfterAndIsDeletedFalseOrderByCreatedAtDesc(
            String email, LocalDateTime now
    );
}