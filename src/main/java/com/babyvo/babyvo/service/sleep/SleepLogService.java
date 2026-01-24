package com.babyvo.babyvo.service.sleep;

import com.babyvo.babyvo.common.exception.BusinessException;
import com.babyvo.babyvo.entity.baby.BabyEntity;
import com.babyvo.babyvo.entity.enums.SleepPlace;
import com.babyvo.babyvo.entity.sleep.SleepLogEntity;
import com.babyvo.babyvo.entity.user.UserEntity;
import com.babyvo.babyvo.repository.baby.BabyRepository;
import com.babyvo.babyvo.repository.sleep.SleepLogRepository;
import com.babyvo.babyvo.repository.user.UserRepository;
import com.babyvo.babyvo.response.sleep.ActiveSleepResponse;
import com.babyvo.babyvo.response.sleep.SleepLogResponse;
import com.babyvo.babyvo.response.sleep.SleepSummaryResponse;
import com.babyvo.babyvo.service.baby.BabyAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SleepLogService {

    private final SleepLogRepository sleepLogRepository;
    private final BabyRepository babyRepository;
    private final BabyAccessService babyAccessService;

    @Transactional
    public SleepLogResponse create(UUID babyId, UUID currentUserId,
                                   LocalDateTime startedAt, LocalDateTime endedAt,
                                   SleepPlace place, String note) {

        babyAccessService.requireWriteParent(babyId, currentUserId);

        BabyEntity baby = babyRepository.findById(babyId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "BABY_NOT_FOUND"));

        if (endedAt != null && endedAt.isBefore(startedAt)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "SLEEP_TIME_RANGE_INVALID");
        }

        // aktif uyku varken yeni aktif uyku açılmasın
        if (endedAt == null) {
            boolean hasActive = sleepLogRepository.findLatestActiveSleep(babyId).isPresent();
            if (hasActive) {
                throw new BusinessException(HttpStatus.CONFLICT, "SLEEP_ALREADY_ACTIVE");
            }
        }

        SleepLogEntity e = new SleepLogEntity();
        e.setBabyEntity(baby);
        e.setStartedAt(startedAt);
        e.setEndedAt(endedAt);
        e.setPlace(place);
        e.setNote(note);

        sleepLogRepository.save(e);
        return SleepLogResponse.of(e);
    }

    @Transactional
    public SleepLogResponse stop(UUID babyId, UUID sleepId, UUID currentUserId, LocalDateTime endedAt) {
        babyAccessService.requireWriteParent(babyId, currentUserId);

        SleepLogEntity e = sleepLogRepository.findById(sleepId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "SLEEP_NOT_FOUND"));

        if (Boolean.TRUE.equals(e.getIsDeleted())) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "SLEEP_NOT_FOUND");
        }

        if (!e.getBabyEntity().getId().equals(babyId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "SLEEP_BABY_MISMATCH");
        }

        if (e.getEndedAt() != null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "SLEEP_ALREADY_STOPPED");
        }

        LocalDateTime end = (endedAt != null) ? endedAt : LocalDateTime.now();
        if (end.isBefore(e.getStartedAt())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "SLEEP_TIME_RANGE_INVALID");
        }

        e.setEndedAt(end);
        return SleepLogResponse.of(e);
    }

    @Transactional
    public SleepLogResponse update(UUID babyId, UUID sleepId, UUID currentUserId,
                                   LocalDateTime startedAt, LocalDateTime endedAt,
                                   SleepPlace place, String note) {

        babyAccessService.requireWriteParent(babyId, currentUserId);

        SleepLogEntity e = sleepLogRepository.findById(sleepId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "SLEEP_NOT_FOUND"));

        if (Boolean.TRUE.equals(e.getIsDeleted())) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "SLEEP_NOT_FOUND");
        }

        if (!e.getBabyEntity().getId().equals(babyId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "SLEEP_BABY_MISMATCH");
        }

        LocalDateTime newStart = (startedAt != null) ? startedAt : e.getStartedAt();
        LocalDateTime newEnd = (endedAt != null) ? endedAt : e.getEndedAt();

        if (newEnd != null && newEnd.isBefore(newStart)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "SLEEP_TIME_RANGE_INVALID");
        }

        if (startedAt != null) e.setStartedAt(startedAt);
        if (endedAt != null) e.setEndedAt(endedAt);
        if (place != null) e.setPlace(place);
        if (note != null) e.setNote(note);

        return SleepLogResponse.of(e);
    }

    @Transactional
    public void delete(UUID babyId, UUID sleepId, UUID currentUserId) {
        babyAccessService.requireWriteParent(babyId, currentUserId);

        SleepLogEntity e = sleepLogRepository.findById(sleepId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "SLEEP_NOT_FOUND"));

        if (!e.getBabyEntity().getId().equals(babyId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "SLEEP_BABY_MISMATCH");
        }

        if (Boolean.TRUE.equals(e.getIsDeleted())) return; // idempotent
        e.setIsDeleted(true);
    }

    @Transactional(readOnly = true)
    public List<SleepLogResponse> list(UUID babyId, UUID currentUserId, LocalDate from, LocalDate to) {
        babyAccessService.requireActiveParent(babyId, currentUserId);

        LocalDate start = (from != null) ? from : LocalDate.now();
        LocalDate end = (to != null) ? to : start;

        LocalDateTime fromDt = start.atStartOfDay();
        LocalDateTime toDt = end.plusDays(1).atStartOfDay();

        return sleepLogRepository.findByBabyAndStartedAtRange(babyId, fromDt, toDt)
                .stream().map(SleepLogResponse::of).toList();
    }

    @Transactional(readOnly = true)
    public SleepSummaryResponse dailySummary(UUID babyId, UUID currentUserId, LocalDate date) {
        babyAccessService.requireActiveParent(babyId, currentUserId);

        LocalDate d = (date != null) ? date : LocalDate.now();
        LocalDateTime from = d.atStartOfDay();
        LocalDateTime to = d.plusDays(1).atStartOfDay();

        List<SleepLogEntity> logs = sleepLogRepository.findByBabyAndStartedAtRange(babyId, from, to);

        long totalMinutes = logs.stream()
                .filter(x -> x.getEndedAt() != null)
                .mapToLong(x -> Duration.between(x.getStartedAt(), x.getEndedAt()).toMinutes())
                .sum();

        int sessionCount = (int) logs.stream().filter(x -> x.getEndedAt() != null).count();

        return new SleepSummaryResponse(babyId, d, totalMinutes, sessionCount);
    }

    @Transactional(readOnly = true)
    public ActiveSleepResponse getActiveSleep(UUID babyId, UUID currentUserId) {
        babyAccessService.requireActiveParent(babyId, currentUserId);

        return sleepLogRepository.findLatestActiveSleep(babyId)
                .map(ActiveSleepResponse::of)
                .orElse(null);
    }
}