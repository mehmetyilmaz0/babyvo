package com.babyvo.babyvo.controller.auth;

import com.babyvo.babyvo.common.apis.ApiResult;
import com.babyvo.babyvo.entity.user.UserEntity;
import com.babyvo.babyvo.request.auth.StartEmailOtpRequest;
import com.babyvo.babyvo.request.auth.VerifyEmailOtpRequest;
import com.babyvo.babyvo.response.auth.AuthTokensResponse;
import com.babyvo.babyvo.response.auth.StartEmailOtpResponse;
import com.babyvo.babyvo.service.auth.EmailAuthService;
import com.babyvo.babyvo.service.auth.EmailOtpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth/email")
@RequiredArgsConstructor
public class AuthEmailController {

    private final EmailOtpService emailOtpService;
    private final EmailAuthService emailAuthService;

    @PostMapping("/start")
    public ApiResult<StartEmailOtpResponse> start(@Valid @RequestBody StartEmailOtpRequest req,
                                       HttpServletRequest httpReq) {
        String ip = httpReq.getRemoteAddr();
        String userAgent = httpReq.getHeader("UserEntity-Agent");

        UUID otpRef = emailOtpService.start(req.email(), ip, userAgent);
        return ApiResult.ok(new StartEmailOtpResponse(otpRef.toString(), 180));
    }

    @PostMapping("/verify")
    public ApiResult<AuthTokensResponse> verify(@Valid @RequestBody VerifyEmailOtpRequest req) {
        UUID otpRef = UUID.fromString(req.otpRef());

        // OTP doğrula (consumed eder)
        emailOtpService.verifyOrThrow(otpRef, req.otp());

        // OTP kaydından email'i çekmek için
        String email = emailOtpService.getEmailByOtpRef(otpRef);

        UserEntity user = emailAuthService.findOrCreateEmailUser(email);

        String access = emailAuthService.accessToken(user);
        String refresh = emailAuthService.refreshToken(user);

        return ApiResult.ok(new AuthTokensResponse(
                access,
                refresh,
                new AuthTokensResponse.UserInfo(user.getId(), user.getPrimaryEmail()))
        );
    }
}