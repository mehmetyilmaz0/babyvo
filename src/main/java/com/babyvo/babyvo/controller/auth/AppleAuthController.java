package com.babyvo.babyvo.controller.auth;

import com.babyvo.babyvo.common.apis.ApiResult;
import com.babyvo.babyvo.request.auth.AppleLoginRequest;
import com.babyvo.babyvo.response.auth.AuthTokensResponse;
import com.babyvo.babyvo.service.auth.AppleAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/apple")
@RequiredArgsConstructor
public class AppleAuthController {

    private final AppleAuthService appleAuthService;

    @PostMapping("/login")
    public ApiResult<AuthTokensResponse> login(@Valid @RequestBody AppleLoginRequest req) {
        return ApiResult.ok(appleAuthService.login(req.idToken()));
    }
}