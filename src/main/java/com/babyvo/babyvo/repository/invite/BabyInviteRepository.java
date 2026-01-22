package com.babyvo.babyvo.repository.invite;

import com.babyvo.babyvo.entity.invite.BabyInviteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BabyInviteRepository extends JpaRepository<BabyInviteEntity, UUID> {
    Optional<BabyInviteEntity> findByTokenHash(String tokenHash);
}
