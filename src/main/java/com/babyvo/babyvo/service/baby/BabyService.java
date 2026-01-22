package com.babyvo.babyvo.service.baby;

import com.babyvo.babyvo.common.exception.BusinessException;
import com.babyvo.babyvo.entity.baby.BabyEntity;
import com.babyvo.babyvo.entity.baby.BabyParentEntity;
import com.babyvo.babyvo.entity.enums.BabyParentRole;
import com.babyvo.babyvo.entity.enums.BabyParentStatus;
import com.babyvo.babyvo.entity.user.UserEntity;
import com.babyvo.babyvo.repository.baby.BabyParentRepository;
import com.babyvo.babyvo.repository.baby.BabyRepository;
import com.babyvo.babyvo.repository.user.UserRepository;
import com.babyvo.babyvo.request.baby.CreateBabyRequest;
import com.babyvo.babyvo.request.baby.UpdateBabyRequest;
import com.babyvo.babyvo.response.baby.BabyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BabyService {

    private final BabyRepository babyRepository;
    private final BabyParentRepository babyParentRepository;
    private final UserRepository userRepository;

    @Transactional
    public BabyResponse create(CreateBabyRequest req, UUID currentUserId) {
        UserEntity user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND"));

        BabyEntity baby = new BabyEntity();
        baby.setName(req.name());
        baby.setBirthDate(req.birthDate());
        baby.setSex(req.sex());
        baby.setCreatedByUserEntity(user);
        baby.setCreatedAt(LocalDateTime.now());
        baby.setCreatedBy(user.getPrimaryEmail());

        baby = babyRepository.save(baby);

        BabyParentEntity rel = new BabyParentEntity();
        rel.setBabyEntity(baby);
        rel.setUserEntity(user);
        rel.setRole(BabyParentRole.OWNER);
        rel.setStatus(BabyParentStatus.ACTIVE);

        babyParentRepository.save(rel);

        return new BabyResponse(baby.getId(), baby.getName(), baby.getBirthDate(), baby.getSex());
    }

    @Transactional(readOnly = true)
    public List<BabyResponse> listMine(UUID currentUserId) {
        // User -> active parents -> baby list
        return babyParentRepository.findAllByUserEntity_IdAndStatus(currentUserId, BabyParentStatus.ACTIVE)
                .stream()
                .map(bp -> {
                    BabyEntity b = bp.getBabyEntity();
                    return new BabyResponse(b.getId(), b.getName(), b.getBirthDate(), b.getSex());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public BabyResponse getMineById(UUID babyId, UUID currentUserId) {

        // Kullanıcı var mı? (sen create'de kontrol ediyorsun, burada da tutarlı olsun)
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND"));

        // Bu kullanıcı bu bebeğe ACTIVE parent mı?
        boolean hasAccess = babyParentRepository
                .existsByBabyEntity_IdAndUserEntity_IdAndStatus(babyId, currentUserId, BabyParentStatus.ACTIVE);

        if (!hasAccess) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "BABY_ACCESS_DENIED");
        }

        BabyEntity baby = babyRepository.findById(babyId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "BABY_NOT_FOUND"));

        return new BabyResponse(baby.getId(), baby.getName(), baby.getBirthDate(), baby.getSex());
    }

    public BabyResponse update(UUID babyId, @Valid UpdateBabyRequest req, UUID currentUserId) {
        UserEntity user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND"));

        // Bu kullanıcı bu bebeğe ACTIVE parent mı?
        boolean hasAccess = babyParentRepository
                .existsByBabyEntity_IdAndUserEntity_IdAndStatus(babyId, currentUserId, BabyParentStatus.ACTIVE);

        if (!hasAccess) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "BABY_ACCESS_DENIED");
        }

        BabyEntity baby = babyRepository.findById(babyId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "BABY_NOT_FOUND"));

        req.name().ifPresent(baby::setName);
        req.birthDate().ifPresent(baby::setBirthDate);
        req.sex().ifPresent(baby::setSex);

        babyRepository.save(baby);

        return new BabyResponse(baby.getId(), baby.getName(), baby.getBirthDate(), baby.getSex());
    }
}