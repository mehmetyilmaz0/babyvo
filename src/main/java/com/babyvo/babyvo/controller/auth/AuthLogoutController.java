package com.babyvo.babyvo.controller.auth;

import com.babyvo.babyvo.common.apis.ApiResult;
import com.babyvo.babyvo.request.auth.LogoutRequest;
import com.babyvo.babyvo.service.auth.AuthTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthLogoutController {

    private final AuthTokenService authTokenService;

    @PostMapping("/logout")
    public ApiResult<Void> logout(@RequestBody @Valid LogoutRequest req) {
        authTokenService.logout(req.refreshToken());
        return ApiResult.ok(null);
    }
}