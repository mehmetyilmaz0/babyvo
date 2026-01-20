package com.babyvo.babyvo.controller.user;

import com.babyvo.babyvo.common.apis.ApiResult;
import com.babyvo.babyvo.common.exception.BusinessException;
import com.babyvo.babyvo.entity.user.UserEntity;
import com.babyvo.babyvo.repository.user.UserRepository;
import com.babyvo.babyvo.response.user.MeResponse;
import com.babyvo.babyvo.service.auth.AuthTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final AuthTokenService authTokenService;

    @GetMapping("/me")
    public ApiResult<MeResponse> me(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        return ApiResult.ok(new MeResponse(user.getId(), user.getPrimaryEmail()));
    }

    @PostMapping("/me/logout-all")
    public ApiResult<Void> logoutAllDevices(Authentication authentication) {
        UUID userId = extractUserId(authentication);
        authTokenService.logoutAllDevices(userId);
        return ApiResult.ok(null);
    }

    private UUID extractUserId(Authentication authentication) {
        // Senin projede UserIdAuthenticationToken var demiştin.
        // En temiz yol: token’dan userId çekmek.

        if (authentication instanceof com.babyvo.babyvo.config.security.UserIdAuthenticationToken t) {
            return t.getUserId();
        }

        // Fallback: name subject olabilir
        return UUID.fromString(authentication.getName());
    }
}