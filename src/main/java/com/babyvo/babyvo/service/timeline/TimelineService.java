package com.babyvo.babyvo.service.timeline;

import com.babyvo.babyvo.entity.feeding.FeedingLogEntity;
import com.babyvo.babyvo.repository.feeding.FeedingLogRepository;
import com.babyvo.babyvo.response.feeding.FeedingLogResponse;
import com.babyvo.babyvo.response.timeline.TimelineItemResponse;
import com.babyvo.babyvo.response.timeline.TimelineResponse;
import com.babyvo.babyvo.service.baby.BabyAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TimelineService {

    private final BabyAccessService babyAccessService;
    private final FeedingLogRepository feedingLogRepository;
    // TODO: diaperLogRepository, sleepLogRepository (ekleyince)

    @Transactional(readOnly = true)
    public TimelineResponse getDailyTimeline(UUID babyId, UUID currentUserId, LocalDate date) {
        babyAccessService.requireWriteParent(babyId, currentUserId);

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);

        // Şimdilik feeding çekiyoruz (diğerleri eklenince buraya eklenecek)
        Pageable pageable = PageRequest.of(0, 500, Sort.by(Sort.Direction.DESC, "loggedAt"));
        List<FeedingLogEntity> feedings = feedingLogRepository
                .findByBabyEntity_IdAndIsDeletedFalseAndLoggedAtBetweenOrderByLoggedAtDesc(babyId, start, end, pageable)
                .getContent();

        List<TimelineItemResponse> items = new ArrayList<>();

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
            items.add(new TimelineItemResponse("FEEDING", f.getId(), f.getLoggedAt(), data));
        }

        // loggedAt desc zaten, ama ileride multiple kaynak merge edince sort şart
        items.sort(Comparator.comparing(TimelineItemResponse::loggedAt).reversed());

        return new TimelineResponse(babyId, date, items);
    }
}