package com.babyvo.babyvo.service.auth;

import com.babyvo.babyvo.entity.enums.IdentityProvider;
import com.babyvo.babyvo.entity.user.UserEntity;
import com.babyvo.babyvo.entity.user.UserIdentityEntity;
import com.babyvo.babyvo.repository.user.UserIdentityRepository;
import com.babyvo.babyvo.repository.user.UserRepository;
import com.babyvo.babyvo.service.auth.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmailAuthService {

    private final UserRepository userRepository;
    private final UserIdentityRepository identityRepository;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public UserEntity findOrCreateEmailUser(String normalizedEmail) {
        return identityRepository.findEmailIdentityWithUser(IdentityProvider.EMAIL, normalizedEmail)
                .map(UserIdentityEntity::getUserEntity)
                .orElseGet(() -> {
                    UserEntity user = new UserEntity();
                    user.setPrimaryEmail(normalizedEmail);
                    UserEntity savedUser = userRepository.save(user);

                    UserIdentityEntity identity = new UserIdentityEntity();
                    identity.setUserEntity(savedUser);
                    identity.setProvider(IdentityProvider.EMAIL);
                    identity.setProviderSubject(normalizedEmail);
                    identity.setEmail(normalizedEmail);
                    identity.setEmailVerified(true);

                    identityRepository.save(identity);
                    return savedUser;
                });
    }

    public String accessToken(UserEntity user) {
        return jwtService.createAccessToken(user.getId());
    }

    public JwtService.IssuedRefreshToken issueRefreshToken(UserEntity user) {
        return jwtService.issueRefreshToken(user.getId());
    }
}