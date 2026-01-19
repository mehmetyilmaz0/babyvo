package com.babyvo.babyvo.controller.user;

import com.babyvo.babyvo.common.apis.ApiResult;
import com.babyvo.babyvo.entity.user.UserEntity;
import com.babyvo.babyvo.repository.user.UserRepository;
import com.babyvo.babyvo.response.user.MeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ApiResult<MeResponse> me(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));

        return ApiResult.ok(new MeResponse(user.getId(), user.getPrimaryEmail()));
    }
}