package com.babyvo.babyvo.service.baby;

import com.babyvo.babyvo.common.exception.BusinessException;
import com.babyvo.babyvo.entity.enums.BabyParentStatus;
import com.babyvo.babyvo.entity.enums.BabyPermission;
import com.babyvo.babyvo.repository.baby.BabyParentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BabyAccessService {

    private final BabyParentRepository babyParentRepository;

    public void requireActiveParent(UUID babyId, UUID userId) {
        boolean ok = babyParentRepository.existsByBabyEntity_IdAndUserEntity_IdAndStatus(
                babyId, userId, BabyParentStatus.ACTIVE
        );
        if (!ok) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "BABY_ACCESS_DENIED");
        }
    }

    public void requireWriteParent(UUID babyId, UUID userId) {
        // ACTIVE + READ_WRITE şart
        boolean ok = babyParentRepository.existsByBabyEntity_IdAndUserEntity_IdAndStatusAndPermission(
                babyId, userId, BabyParentStatus.ACTIVE, BabyPermission.READ_WRITE
        );
        if (!ok) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "BABY_WRITE_ACCESS_DENIED");
        }
    }
}