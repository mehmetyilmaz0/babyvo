package com.babyvo.babyvo.controller.timeline;

import com.babyvo.babyvo.common.apis.ApiResult;
import com.babyvo.babyvo.config.security.CurrentUser;
import com.babyvo.babyvo.response.timeline.TimelineResponse;
import com.babyvo.babyvo.service.timeline.TimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/babies/{babyId}/timeline")
@RequiredArgsConstructor
public class TimelineController {

    private final TimelineService timelineService;

    @GetMapping
    public ApiResult<TimelineResponse> get(
            @PathVariable UUID babyId,
            @RequestParam LocalDate date,
            @CurrentUser UUID currentUserId
    ) {
        return ApiResult.ok(timelineService.getDailyTimeline(babyId, currentUserId, date));
    }
}