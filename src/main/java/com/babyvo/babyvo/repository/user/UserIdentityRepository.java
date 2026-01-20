package com.babyvo.babyvo.repository.user;

import com.babyvo.babyvo.entity.enums.IdentityProvider;
import com.babyvo.babyvo.entity.user.UserIdentityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserIdentityRepository extends JpaRepository<UserIdentityEntity, UUID> {

    Optional<UserIdentityEntity> findByProviderAndProviderSubjectAndIsDeletedFalse(IdentityProvider provider, String providerSubject);

    default Optional<UserIdentityEntity> findEmailIdentity(String normalizedEmail) {
        return findByProviderAndProviderSubjectAndIsDeletedFalse(IdentityProvider.EMAIL, normalizedEmail);
    }
    Optional<UserIdentityEntity> findByProviderAndProviderSubject(IdentityProvider provider, String providerSubject);

    boolean existsByProviderAndProviderSubject(IdentityProvider provider, String providerSubject);

    boolean existsByProviderAndEmail(IdentityProvider provider, String email);
}