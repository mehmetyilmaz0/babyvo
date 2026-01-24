package com.babyvo.babyvo.controller.sleep;

import com.babyvo.babyvo.common.apis.ApiResult;
import com.babyvo.babyvo.config.security.CurrentUser;
import com.babyvo.babyvo.request.sleep.CreateSleepLogRequest;
import com.babyvo.babyvo.request.sleep.StopSleepRequest;
import com.babyvo.babyvo.request.sleep.UpdateSleepLogRequest;
import com.babyvo.babyvo.response.sleep.ActiveSleepResponse;
import com.babyvo.babyvo.response.sleep.SleepLogResponse;
import com.babyvo.babyvo.response.sleep.SleepSummaryResponse;
import com.babyvo.babyvo.service.sleep.SleepLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/babies/{babyId}/sleeps")
public class SleepController {

    private final SleepLogService sleepLogService;

    @PostMapping
    public ApiResult<SleepLogResponse> create(
            @PathVariable UUID babyId,
            @CurrentUser UUID userId,
            @Valid @RequestBody CreateSleepLogRequest req
    ) {
        return ApiResult.ok(
                sleepLogService.create(
                        babyId,
                        userId,
                        req.startedAt(),
                        req.endedAt(),
                        req.place(),
                        req.note()
                )
        );
    }

    @PostMapping("/{sleepId}/stop")
    public ApiResult<SleepLogResponse> stop(
            @PathVariable UUID babyId,
            @PathVariable UUID sleepId,
            @CurrentUser UUID userId,
            @RequestBody(required = false) StopSleepRequest req
    ) {
        return ApiResult.ok(
                sleepLogService.stop(
                        babyId,
                        sleepId,
                        userId,
                        req != null ? req.endedAt() : null
                )
        );
    }

    @PatchMapping("/{sleepId}")
    public ApiResult<SleepLogResponse> update(
            @PathVariable UUID babyId,
            @PathVariable UUID sleepId,
            @CurrentUser UUID userId,
            @RequestBody UpdateSleepLogRequest req
    ) {
        return ApiResult.ok(
                sleepLogService.update(
                        babyId,
                        sleepId,
                        userId,
                        req.startedAt(),
                        req.endedAt(),
                        req.place(),
                        req.note()
                )
        );
    }

    @DeleteMapping("/{sleepId}")
    public ApiResult<Void> delete(
            @PathVariable UUID babyId,
            @PathVariable UUID sleepId,
            @CurrentUser UUID userId
    ) {
        sleepLogService.delete(babyId, sleepId, userId);
        return ApiResult.ok(null);
    }

    @GetMapping
    public ApiResult<List<SleepLogResponse>> list(
            @PathVariable UUID babyId,
            @CurrentUser UUID userId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        return ApiResult.ok(sleepLogService.list(babyId, userId, from, to));
    }

    @GetMapping("/summary")
    public ApiResult<SleepSummaryResponse> summary(
            @PathVariable UUID babyId,
            @CurrentUser UUID userId,
            @RequestParam(required = false) LocalDate date
    ) {
        return ApiResult.ok(sleepLogService.dailySummary(babyId, userId, date));
    }

    @GetMapping("/active")
    public ApiResult<ActiveSleepResponse> active(@PathVariable UUID babyId, @CurrentUser UUID currentUserId) {
        return ApiResult.ok(sleepLogService.getActiveSleep(babyId, currentUserId));
    }
}