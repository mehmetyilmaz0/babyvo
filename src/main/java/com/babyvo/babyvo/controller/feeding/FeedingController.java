package com.babyvo.babyvo.controller.feeding;

import com.babyvo.babyvo.common.apis.ApiResult;
import com.babyvo.babyvo.config.security.CurrentUser;
import com.babyvo.babyvo.request.feeding.CreateFeedingLogRequest;
import com.babyvo.babyvo.request.feeding.UpdateFeedingLogRequest;
import com.babyvo.babyvo.response.feeding.FeedingLogResponse;
import com.babyvo.babyvo.service.feeding.FeedingLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/babies/{babyId}/feedings")
@RequiredArgsConstructor
public class FeedingController {

    private final FeedingLogService feedingLogService;

    @PostMapping
    public ApiResult<FeedingLogResponse> create(
            @PathVariable UUID babyId,
            @CurrentUser UUID currentUserId,
            @Valid @RequestBody CreateFeedingLogRequest req
    ) {
        return ApiResult.ok(feedingLogService.create(babyId, currentUserId, req));
    }

    @GetMapping
    public ApiResult<Page<FeedingLogResponse>> list(
            @PathVariable UUID babyId,
            @CurrentUser UUID currentUserId,
            @RequestParam LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResult.ok(feedingLogService.listByDate(babyId, currentUserId, date, page, size));
    }

    @PatchMapping("/{feedingId}")
    public ApiResult<FeedingLogResponse> update(
            @PathVariable UUID babyId,
            @PathVariable UUID feedingId,
            @CurrentUser UUID currentUserId,
            @RequestBody @Valid UpdateFeedingLogRequest req
    ) {
        return ApiResult.ok(feedingLogService.update(babyId, feedingId, currentUserId, req));
    }

    @DeleteMapping("/{feedingId}")
    public ApiResult<Void> delete(
            @PathVariable UUID babyId,
            @PathVariable UUID feedingId,
            @CurrentUser UUID currentUserId
    ) {
        feedingLogService.softDelete(babyId, feedingId, currentUserId);
        return ApiResult.ok(null);
    }

    @GetMapping("/summary/breast")
    public ApiResult<com.babyvo.babyvo.response.feeding.BreastSummaryResponse> breastSummary(
            @PathVariable UUID babyId,
            @RequestParam java.time.LocalDate date,
            @CurrentUser UUID currentUserId
    ) {
        return ApiResult.ok(feedingLogService.getBreastSummary(babyId, currentUserId, date));
    }
}