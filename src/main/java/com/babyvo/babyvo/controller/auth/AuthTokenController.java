package com.babyvo.babyvo.controller.auth;

import com.babyvo.babyvo.common.apis.ApiResult;
import com.babyvo.babyvo.request.auth.RefreshTokenRequest;
import com.babyvo.babyvo.response.auth.AuthTokensResponse;
import com.babyvo.babyvo.service.auth.AuthTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/token")
public class AuthTokenController {

    private final AuthTokenService authTokenService;

    @PostMapping("/refresh")
    public ApiResult<AuthTokensResponse> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        return ApiResult.ok(authTokenService.refresh(request.refreshToken()));
    }
}