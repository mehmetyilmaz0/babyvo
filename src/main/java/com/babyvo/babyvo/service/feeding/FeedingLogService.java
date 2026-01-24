package com.babyvo.babyvo.service.feeding;

import com.babyvo.babyvo.common.exception.BusinessException;
import com.babyvo.babyvo.entity.baby.BabyEntity;
import com.babyvo.babyvo.entity.enums.FeedingType;
import com.babyvo.babyvo.entity.feeding.FeedingLogEntity;
import com.babyvo.babyvo.entity.user.UserEntity;
import com.babyvo.babyvo.repository.baby.BabyRepository;
import com.babyvo.babyvo.repository.feeding.FeedingLogRepository;
import com.babyvo.babyvo.repository.user.UserRepository;
import com.babyvo.babyvo.request.feeding.CreateFeedingLogRequest;
import com.babyvo.babyvo.request.feeding.UpdateFeedingLogRequest;
import com.babyvo.babyvo.response.feeding.BreastSummaryResponse;
import com.babyvo.babyvo.response.feeding.FeedingLogResponse;
import com.babyvo.babyvo.service.baby.BabyAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedingLogService {

    private final FeedingLogRepository feedingLogRepository;
    private final BabyRepository babyRepository;
    private final UserRepository userRepository;
    private final BabyAccessService babyAccessService;

    @Transactional
    public FeedingLogResponse create(UUID babyId, UUID currentUserId, CreateFeedingLogRequest req) {
        babyAccessService.requireWriteParent(babyId, currentUserId);

        validate(req);

        BabyEntity baby = babyRepository.findById(babyId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "BABY_NOT_FOUND"));

        UserEntity user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND"));

        FeedingLogEntity e = new FeedingLogEntity();
        e.setBabyEntity(baby);
        e.setCreatedByUserEntity(user);
        e.setType(req.type());
        e.setBreastSide(req.breastSide());
        e.setDurationSeconds(req.durationSeconds());
        e.setAmountMl(req.amountMl());
        e.setLoggedAt(req.loggedAt());
        e.setNote(req.note());

        FeedingLogEntity saved = feedingLogRepository.save(e);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<FeedingLogResponse> listByDate(UUID babyId, UUID currentUserId, LocalDate date, int page, int size) {
        babyAccessService.requireWriteParent(babyId, currentUserId);

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "loggedAt"));
        return feedingLogRepository
                .findByBabyEntity_IdAndIsDeletedFalseAndLoggedAtBetweenOrderByLoggedAtDesc(babyId, start, end, pageable)
                .map(this::toResponse);
    }

    private void validate(CreateFeedingLogRequest req) {
        if (req.type() == FeedingType.BREAST) {
            if (req.breastSide() == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "BREAST_SIDE_REQUIRED");
            }
            if (req.durationSeconds() == null || req.durationSeconds() <= 0) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "DURATION_REQUIRED");
            }
            if (req.amountMl() != null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "AMOUNT_NOT_ALLOWED_FOR_BREAST");
            }
        }

        if (req.type() == FeedingType.BOTTLE) {
            if (req.amountMl() == null || req.amountMl() <= 0) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "AMOUNT_REQUIRED");
            }
            if (req.breastSide() != null || req.durationSeconds() != null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "BREAST_FIELDS_NOT_ALLOWED_FOR_BOTTLE");
            }
        }
    }

    @Transactional
    public FeedingLogResponse update(UUID babyId, UUID feedingId, UUID currentUserId, UpdateFeedingLogRequest req) {
        babyAccessService.requireWriteParent(babyId, currentUserId);

        FeedingLogEntity e = feedingLogRepository.findByIdAndIsDeletedFalse(feedingId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "FEEDING_NOT_FOUND"));

        // babyId match
        if (!e.getBabyEntity().getId().equals(babyId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "FEEDING_NOT_FOUND");
        }

        // PATCH apply (null gelmeyeni set et)
        if (req.type() != null) e.setType(req.type());
        if (req.breastSide() != null) e.setBreastSide(req.breastSide());
        if (req.durationSeconds() != null) e.setDurationSeconds(req.durationSeconds());
        if (req.amountMl() != null) e.setAmountMl(req.amountMl());
        if (req.loggedAt() != null) e.setLoggedAt(req.loggedAt());
        if (req.note() != null) e.setNote(req.note());

        // tekrar validate (type’a göre alanlar uyumlu mu)
        validateForEntity(e);

        return toResponse(e);
    }

    @Transactional
    public void softDelete(UUID babyId, UUID feedingId, UUID currentUserId) {
        babyAccessService.requireWriteParent(babyId, currentUserId);

        FeedingLogEntity e = feedingLogRepository.findByIdAndIsDeletedFalse(feedingId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "FEEDING_NOT_FOUND"));

        if (!e.getBabyEntity().getId().equals(babyId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "FEEDING_NOT_FOUND");
        }

        e.setIsDeleted(true);
    }

    // entity üzerinden validate (mevcut validate(req) yerine bunu da kullan)
    private void validateForEntity(FeedingLogEntity e) {
        if (e.getType() == FeedingType.BREAST) {
            if (e.getBreastSide() == null) throw new BusinessException(HttpStatus.BAD_REQUEST, "BREAST_SIDE_REQUIRED");
            if (e.getDurationSeconds() == null || e.getDurationSeconds() <= 0) throw new BusinessException(HttpStatus.BAD_REQUEST, "DURATION_REQUIRED");
            if (e.getAmountMl() != null) throw new BusinessException(HttpStatus.BAD_REQUEST, "AMOUNT_NOT_ALLOWED_FOR_BREAST");
        } else if (e.getType() == FeedingType.BOTTLE) {
            if (e.getAmountMl() == null || e.getAmountMl() <= 0) throw new BusinessException(HttpStatus.BAD_REQUEST, "AMOUNT_REQUIRED");
            if (e.getBreastSide() != null || e.getDurationSeconds() != null) throw new BusinessException(HttpStatus.BAD_REQUEST, "BREAST_FIELDS_NOT_ALLOWED_FOR_BOTTLE");
        } else {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "FEEDING_TYPE_INVALID");
        }

        if (e.getLoggedAt() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "LOGGED_AT_REQUIRED");
        }
    }

    @Transactional(readOnly = true)
    public BreastSummaryResponse getBreastSummary(UUID babyId, UUID currentUserId, LocalDate date) {
        babyAccessService.requireWriteParent(babyId, currentUserId);

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);

        long left = feedingLogRepository.sumBreastDurationSeconds(babyId, com.babyvo.babyvo.entity.enums.BreastSide.LEFT, start, end);
        long right = feedingLogRepository.sumBreastDurationSeconds(babyId, com.babyvo.babyvo.entity.enums.BreastSide.RIGHT, start, end);

        return new BreastSummaryResponse(babyId, date, left, right, left + right);
    }

    private FeedingLogResponse toResponse(FeedingLogEntity e) {
        return new FeedingLogResponse(
                e.getId(),
                e.getBabyEntity().getId(),
                e.getCreatedByUserEntity().getId(),
                e.getType(),
                e.getBreastSide(),
                e.getDurationSeconds(),
                e.getAmountMl(),
                e.getLoggedAt(),
                e.getNote()
        );
    }
}