package com.babyvo.babyvo.controller.auth;

import com.babyvo.babyvo.common.apis.ApiResult;
import com.babyvo.babyvo.request.auth.GoogleLoginRequest;
import com.babyvo.babyvo.response.auth.AuthTokensResponse;
import com.babyvo.babyvo.service.auth.GoogleAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/google")
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;

    @PostMapping("/login")
    public ApiResult<AuthTokensResponse> login(@RequestBody @Valid GoogleLoginRequest request) {
        return ApiResult.ok(googleAuthService.loginOrRegister(request.idToken()));
    }

    @PostMapping("/link")
    public ApiResult<AuthTokensResponse> link(@RequestBody @Valid GoogleLoginRequest request,
                                              Authentication authentication) {
        return ApiResult.ok(googleAuthService.link(authentication, request.idToken()));
    }
}