package com.babyvo.babyvo.service.invite;

import com.babyvo.babyvo.common.exception.BusinessException;
import com.babyvo.babyvo.entity.baby.BabyEntity;
import com.babyvo.babyvo.entity.baby.BabyParentEntity;
import com.babyvo.babyvo.entity.enums.BabyParentRole;
import com.babyvo.babyvo.entity.enums.BabyParentStatus;
import com.babyvo.babyvo.entity.invite.BabyInviteEntity;
import com.babyvo.babyvo.entity.enums.InviteStatus;
import com.babyvo.babyvo.entity.user.UserEntity;
import com.babyvo.babyvo.repository.baby.BabyParentRepository;
import com.babyvo.babyvo.repository.baby.BabyRepository;
import com.babyvo.babyvo.repository.invite.BabyInviteRepository;
import com.babyvo.babyvo.repository.user.UserRepository;
import com.babyvo.babyvo.request.invite.CreateInviteRequest;
import com.babyvo.babyvo.response.invite.AcceptInviteResponse;
import com.babyvo.babyvo.response.invite.CreateInviteResponse;
import com.babyvo.babyvo.service.baby.BabyAccessService;
import com.babyvo.babyvo.service.mail.EmailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BabyInviteService {


    private final BabyRepository babyRepository;
    private final BabyInviteRepository babyInviteRepository;
    private final UserRepository userRepository;
    private final BabyParentRepository babyParentRepository;
    private final BabyAccessService babyAccessService;

    private final InviteTokenGenerator inviteTokenGenerator;
    private final InviteTokenHasher inviteTokenHasher;

    private final EmailSender emailSender;

    private static final long DEFAULT_EXPIRE_HOURS = 168; // 7 gün

    @Transactional
    public CreateInviteResponse create(UUID babyId, UUID currentUserId, CreateInviteRequest req) {
        babyAccessService.requireWriteParent(babyId, currentUserId);

        BabyEntity baby = babyRepository.findById(babyId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "BABY_NOT_FOUND"));

        UserEntity inviter = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND"));

        String token = inviteTokenGenerator.generate();       // plain
        String tokenHash = inviteTokenHasher.hash(token);     // db

        BabyInviteEntity invite = new BabyInviteEntity();
        invite.setBabyEntity(baby);
        invite.setInvitedEmail(req.email()); // nullable
        invite.setTokenHash(tokenHash);
        invite.setExpiresAt(LocalDateTime.now().plusHours(DEFAULT_EXPIRE_HOURS));
        invite.setStatus(InviteStatus.PENDING);
        invite.setPermission(req.permission());
        invite.setCreatedByUserEntity(inviter);

        try {
            babyInviteRepository.save(invite);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(HttpStatus.CONFLICT, "INVITE_TOKEN_CONFLICT");
        }

        boolean emailSent = false;

        if (req.email() != null && !req.email().isBlank()) {
            emailSender.sendInvite(
                    req.email(),
                    baby.getName(),
                    token,
                    DEFAULT_EXPIRE_HOURS
            );
            emailSent = true;
        }

        return new CreateInviteResponse(token, DEFAULT_EXPIRE_HOURS, emailSent);
    }

    @Transactional
    public AcceptInviteResponse accept(String token, UUID currentUserId) {
        String tokenHash = inviteTokenHasher.hash(token);

        BabyInviteEntity invite = babyInviteRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "INVITE_NOT_FOUND"));

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVITE_NOT_PENDING");
        }

        LocalDateTime now = LocalDateTime.now();
        if (invite.isExpired(now)) {
            invite.setStatus(InviteStatus.EXPIRED);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVITE_EXPIRED");
        }

        UserEntity user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND"));

        if (invite.getInvitedEmail() != null) {
            String userEmail = user.getPrimaryEmail();
            if (userEmail == null || !userEmail.equalsIgnoreCase(invite.getInvitedEmail())) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "INVITE_EMAIL_MISMATCH");
            }
        }

        UUID babyId = invite.getBabyEntity().getId();

        boolean alreadyActive = babyParentRepository
                .existsByBabyEntity_IdAndUserEntity_IdAndStatus(babyId, currentUserId, BabyParentStatus.ACTIVE);

        if (!alreadyActive) {
            BabyParentEntity rel = new BabyParentEntity();
            rel.setBabyEntity(invite.getBabyEntity());
            rel.setUserEntity(user);
            rel.setStatus(BabyParentStatus.ACTIVE);
            rel.setPermission(invite.getPermission());
            rel.setRole(BabyParentRole.PARENT);
            babyParentRepository.save(rel);
        }

        invite.setStatus(InviteStatus.ACCEPTED);
        invite.setAcceptedByUserEntity(user);
        invite.setAcceptedAt(now);

        return new AcceptInviteResponse(babyId, BabyParentStatus.ACTIVE);
    }

    @Transactional
    public void reject(String token, UUID currentUserId) {
        String tokenHash = inviteTokenHasher.hash(token);

        BabyInviteEntity invite = babyInviteRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "INVITE_NOT_FOUND"));

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVITE_NOT_PENDING");
        }

        LocalDateTime now = LocalDateTime.now();
        if (invite.isExpired(now)) {
            invite.setStatus(InviteStatus.EXPIRED);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVITE_EXPIRED");
        }

        UserEntity user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND"));

        if (invite.getInvitedEmail() != null) {
            String userEmail = user.getPrimaryEmail();
            if (userEmail == null || !userEmail.equalsIgnoreCase(invite.getInvitedEmail())) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "INVITE_EMAIL_MISMATCH");
            }
        }

        invite.setStatus(InviteStatus.REJECTED);
    }
}