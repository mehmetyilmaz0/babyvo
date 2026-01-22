package com.babyvo.babyvo.controller.baby;

import com.babyvo.babyvo.common.apis.ApiResult;
import com.babyvo.babyvo.request.baby.CreateBabyRequest;
import com.babyvo.babyvo.request.baby.UpdateBabyRequest;
import com.babyvo.babyvo.response.baby.BabyResponse;
import com.babyvo.babyvo.service.baby.BabyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/babies")
@RequiredArgsConstructor
public class BabyController {

    private final BabyService babyService;

    @PostMapping
    public ApiResult<BabyResponse> create(@Valid @RequestBody CreateBabyRequest req, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResult.ok(babyService.create(req, userId));
    }

    @GetMapping
    public ApiResult<List<BabyResponse>> list(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResult.ok(babyService.listMine(userId));
    }

    @GetMapping("/{babyId}")
    public ApiResult<BabyResponse> getById(@PathVariable UUID babyId, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResult.ok(babyService.getMineById(babyId, userId));
    }

    @PatchMapping("/{babyId}")
    public ApiResult<BabyResponse> update(@PathVariable UUID babyId, @Valid @RequestBody UpdateBabyRequest req, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResult.ok(babyService.update(babyId, req, userId));
    }
}