package com.babyvo.babyvo.controller.auth;

import com.babyvo.babyvo.common.apis.ApiResult;
import com.babyvo.babyvo.entity.user.UserEntity;
import com.babyvo.babyvo.request.auth.StartEmailOtpRequest;
import com.babyvo.babyvo.request.auth.VerifyEmailOtpRequest;
import com.babyvo.babyvo.response.auth.AuthTokensResponse;
import com.babyvo.babyvo.response.auth.StartEmailOtpResponse;
import com.babyvo.babyvo.service.auth.EmailAuthService;
import com.babyvo.babyvo.service.auth.EmailOtpService;
import com.babyvo.babyvo.service.auth.RefreshTokenStore;
import com.babyvo.babyvo.service.auth.jwt.JwtService;
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
    private final JwtService jwtService;
    private final RefreshTokenStore refreshTokenStore;

    @PostMapping("/start")
    public ApiResult<StartEmailOtpResponse> start(@Valid @RequestBody StartEmailOtpRequest req,
                                                  HttpServletRequest httpReq) {
        String ip = httpReq.getRemoteAddr();
        String userAgent = httpReq.getHeader("User-Agent"); // ✅ doğru header

        UUID otpRef = emailOtpService.start(req.email(), ip, userAgent);
        return ApiResult.ok(new StartEmailOtpResponse(otpRef.toString(), 180));
    }

    @PostMapping("/verify")
    public ApiResult<AuthTokensResponse> verify(@Valid @RequestBody VerifyEmailOtpRequest req) {
        UUID otpRef = UUID.fromString(req.otpRef());

        // 1) OTP doğrula (consumed eder)
        emailOtpService.verifyOrThrow(otpRef, req.otp());

        // 2) OTP kaydından email'i çek
        String email = emailOtpService.getEmailByOtpRef(otpRef);

        // 3) User bul/oluştur
        UserEntity user = emailAuthService.findOrCreateEmailUser(email);

        // 4) JWT üret (access + refresh rotation)
        String access = jwtService.createAccessToken(user.getId());

        JwtService.IssuedRefreshToken issuedRefresh = jwtService.issueRefreshToken(user.getId());
        refreshTokenStore.storeActive(
                issuedRefresh.jti(),
                user.getId(),
                issuedRefresh.ttlFromNow()
        );

        return ApiResult.ok(new AuthTokensResponse(
                access,
                issuedRefresh.token(),
                new AuthTokensResponse.UserInfo(user.getId(), user.getPrimaryEmail())
        ));
    }
}