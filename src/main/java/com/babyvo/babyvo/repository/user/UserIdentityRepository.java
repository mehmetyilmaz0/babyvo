package com.babyvo.babyvo.repository.user;

import com.babyvo.babyvo.entity.enums.IdentityProvider;
import com.babyvo.babyvo.entity.user.UserIdentityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserIdentityRepository extends JpaRepository<UserIdentityEntity, UUID> {

    Optional<UserIdentityEntity> findByProviderAndProviderSubjectAndIsDeletedFalse(IdentityProvider provider, String providerSubject);

    @Query("""
select ui
from UserIdentityEntity ui
join fetch ui.userEntity u
where ui.provider = :provider
  and lower(ui.email) = lower(:email)
""")
    Optional<UserIdentityEntity> findEmailIdentityWithUser(IdentityProvider provider, String email);

    Optional<UserIdentityEntity> findByProviderAndProviderSubject(IdentityProvider provider, String providerSubject);

    boolean existsByProviderAndProviderSubject(IdentityProvider provider, String providerSubject);

    boolean existsByProviderAndEmail(IdentityProvider provider, String email);
}