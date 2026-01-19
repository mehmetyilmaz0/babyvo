package com.babyvo.babyvo.service.auth;

import com.babyvo.babyvo.entity.auth.EmailOtpEntity;
import com.babyvo.babyvo.common.exception.BusinessException;
import com.babyvo.babyvo.repository.auth.EmailOtpRepository;
import com.babyvo.babyvo.service.mail.EmailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailOtpService {

    private static final int OTP_EXPIRES_MIN = 3;
    private static final int OTP_MAX_ATTEMPT = 5;
    private static final int OTP_RATE_LIMIT_SECONDS = 60;

    private final EmailOtpRepository emailOtpRepository;
    private final OtpGenerator otpGenerator;
    private final OtpHasher otpHasher;
    private final EmailSender emailSender;

    @Transactional
    public UUID start(String rawEmail, String ip, String userAgent) {
        String email = normalizeEmail(rawEmail);

        // rate limit: son 60 sn içinde kaç OTP
        LocalDateTime after = LocalDateTime.now().minusSeconds(OTP_RATE_LIMIT_SECONDS);
        long recentCount = emailOtpRepository.countByEmailAndCreatedAtAfterAndIsDeletedFalse(email, after);
        if (recentCount > 0) {
            throw BusinessException.badRequest("OTP_TOO_FREQUENT");
        }

        String otp = otpGenerator.generate6Digits();
        String hash = otpHasher.hash(email, otp);

        EmailOtpEntity entity = new EmailOtpEntity();
        entity.setEmail(email);
        entity.setOtpHash(hash);
        entity.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRES_MIN));
        entity.setAttemptCount(0);
        entity.setIp(ip);
        entity.setUserAgent(userAgent);

        EmailOtpEntity saved = emailOtpRepository.save(entity);

        // mail gönder
        emailSender.sendOtp(email, otp, OTP_EXPIRES_MIN);

        return saved.getId();
    }

    @Transactional
    public void verifyOrThrow(UUID otpRef, String otp) {
        EmailOtpEntity entity = emailOtpRepository.findByIdAndIsDeletedFalse(otpRef)
                .orElseThrow(() -> new IllegalArgumentException("OTP_NOT_FOUND"));

        if (entity.isConsumed()) {
            throw BusinessException.badRequest("OTP_ALREADY_USED");
        }
        if (entity.isExpired(LocalDateTime.now())) {
            throw BusinessException.badRequest("OTP_EXPIRED");
        }
        if (entity.getAttemptCount() >= OTP_MAX_ATTEMPT) {
            throw BusinessException.badRequest("OTP_TOO_MANY_ATTEMPTS");
        }

        String expectedHash = otpHasher.hash(entity.getEmail(), otp);
        if (!expectedHash.equals(entity.getOtpHash())) {
            entity.setAttemptCount(entity.getAttemptCount() + 1);
            emailOtpRepository.save(entity);
            throw BusinessException.badRequest("OTP_INVALID");
        }

        entity.setConsumedAt(LocalDateTime.now());
        emailOtpRepository.save(entity);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    @Transactional(readOnly = true)
    public String getEmailByOtpRef(UUID otpRef) {
        return emailOtpRepository.findByIdAndIsDeletedFalse(otpRef)
                .orElseThrow(() -> BusinessException.badRequest("OTP_NOT_FOUND"))
                .getEmail();
    }
}