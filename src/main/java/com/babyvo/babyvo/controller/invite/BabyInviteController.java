package com.babyvo.babyvo.controller.invite;

import com.babyvo.babyvo.common.apis.ApiResult;
import com.babyvo.babyvo.request.invite.AcceptInviteRequest;
import com.babyvo.babyvo.request.invite.CreateInviteRequest;
import com.babyvo.babyvo.request.invite.RejectInviteRequest;
import com.babyvo.babyvo.response.invite.AcceptInviteResponse;
import com.babyvo.babyvo.response.invite.CreateInviteResponse;
import com.babyvo.babyvo.service.invite.BabyInviteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class BabyInviteController {

    private final BabyInviteService babyInviteService;

    @PostMapping("/api/v1/babies/{babyId}/invites")
    public ApiResult<CreateInviteResponse> createInvite(
            @PathVariable UUID babyId,
            @Valid @RequestBody CreateInviteRequest req,
            Authentication auth
    ) {
        UUID currentUserId = UUID.fromString(auth.getName());
        return ApiResult.ok(babyInviteService.create(babyId, currentUserId, req));
    }

    @PostMapping("/api/v1/invites/accept")
    public ApiResult<AcceptInviteResponse> accept(
            @Valid @RequestBody AcceptInviteRequest req,
            Authentication auth
    ) {
        UUID currentUserId = UUID.fromString(auth.getName());
        return ApiResult.ok(babyInviteService.accept(req.inviteToken(), currentUserId));
    }

    @PostMapping("/api/v1/invites/reject")
    public ApiResult<Void> reject(
            @Valid @RequestBody RejectInviteRequest req,
            Authentication auth
    ) {
        UUID currentUserId = UUID.fromString(auth.getName());
        babyInviteService.reject(req.inviteToken(), currentUserId);
        return ApiResult.ok(null);
    }
}