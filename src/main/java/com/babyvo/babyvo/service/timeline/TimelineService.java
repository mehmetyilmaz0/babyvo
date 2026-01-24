package com.babyvo.babyvo.service.timeline;

import com.babyvo.babyvo.entity.feeding.FeedingLogEntity;
import com.babyvo.babyvo.entity.sleep.SleepLogEntity;
import com.babyvo.babyvo.repository.feeding.FeedingLogRepository;
import com.babyvo.babyvo.repository.sleep.SleepLogRepository;
import com.babyvo.babyvo.response.feeding.FeedingLogResponse;
import com.babyvo.babyvo.response.sleep.ActiveSleepResponse;
import com.babyvo.babyvo.response.sleep.SleepLogResponse;
import com.babyvo.babyvo.response.timeline.TimelineItemResponse;
import com.babyvo.babyvo.response.timeline.TimelineResponse;
import com.babyvo.babyvo.response.timeline.TimelineTypes;
import com.babyvo.babyvo.service.baby.BabyAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TimelineService {

    private final BabyAccessService babyAccessService;
    private final FeedingLogRepository feedingLogRepository;
    private final SleepLogRepository sleepLogRepository;
    // TODO: diaperLogRepository

    @Transactional(readOnly = true)
    public TimelineResponse getDailyTimeline(UUID babyId, UUID currentUserId, LocalDate date) {
        // Timeline read -> active parent yeterli
        babyAccessService.requireActiveParent(babyId, currentUserId);

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime endExclusive = date.plusDays(1).atStartOfDay();

        List<TimelineItemResponse> items = new ArrayList<>(512);

        // 1) FEEDING
        Pageable pageable = PageRequest.of(0, 500, Sort.by(Sort.Direction.DESC, "loggedAt"));
        List<FeedingLogEntity> feedings = feedingLogRepository
                .findByBabyEntity_IdAndIsDeletedFalseAndLoggedAtBetweenOrderByLoggedAtDesc(
                        babyId,
                        start,
                        endExclusive,
                        pageable
                )
                .getContent();

        for (FeedingLogEntity f : feedings) {
            FeedingLogResponse data = new FeedingLogResponse(
                    f.getId(),
                    f.getBabyEntity().getId(),
                    f.getCreatedByUserEntity().getId(),
                    f.getType(),
                    f.getBreastSide(),
                    f.getDurationSeconds(),
                    f.getAmountMl(),
                    f.getLoggedAt(),
                    f.getNote()
            );
            items.add(new TimelineItemResponse(TimelineTypes.FEEDING, f.getId(), f.getLoggedAt(), data));
        }

        // 2) SLEEP (sleep_logs’ta “startedAt/endedAt” var, timeline için loggedAt=startedAt)
        List<SleepLogEntity> sleeps = sleepLogRepository.findByBabyAndStartedAtRange(babyId, start, endExclusive);

        for (SleepLogEntity s : sleeps) {
            SleepLogResponse data = SleepLogResponse.of(s);
            // TimelineItemResponse loggedAt -> sleep başlangıcı
            items.add(new TimelineItemResponse(TimelineTypes.SLEEP, s.getId(), s.getStartedAt(), data));
        }

        // 3) TODO: DIAPER ekleyince buraya

        // Merge sonrası kesin sort
        items.sort(Comparator.comparing(TimelineItemResponse::loggedAt).reversed());

        ActiveSleepResponse activeSleep = sleepLogRepository.findLatestActiveSleep(babyId)
                .map(ActiveSleepResponse::of)
                .orElse(null);

        return new TimelineResponse(babyId, date, items, activeSleep);
    }
}