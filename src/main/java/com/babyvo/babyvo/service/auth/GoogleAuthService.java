package com.babyvo.babyvo.service.auth;

import com.babyvo.babyvo.common.exception.BusinessException;
import com.babyvo.babyvo.config.security.UserIdAuthenticationToken;
import com.babyvo.babyvo.entity.enums.IdentityProvider;
import com.babyvo.babyvo.entity.user.UserEntity;
import com.babyvo.babyvo.entity.user.UserIdentityEntity;
import com.babyvo.babyvo.repository.user.UserIdentityRepository;
import com.babyvo.babyvo.repository.user.UserRepository;
import com.babyvo.babyvo.response.auth.AuthTokensResponse;
import com.babyvo.babyvo.service.auth.jwt.JwtService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final GoogleTokenVerifier googleTokenVerifier;
    private final UserRepository userRepository;
    private final UserIdentityRepository userIdentityRepository;
    private final JwtService jwtService;
    private final RefreshTokenStore refreshTokenStore;

    @Transactional
    public AuthTokensResponse loginOrRegister(String idToken) {
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(idToken);

        String providerSubject = payload.getSubject();
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());

        // 1) Google subject ile identity var mı?
        var identityOpt = userIdentityRepository
                .findByProviderAndProviderSubject(IdentityProvider.GOOGLE, providerSubject);

        if (identityOpt.isPresent()) {
            UserEntity user = identityOpt.get().getUserEntity();
            return buildTokens(user);
        }

        // 2) Email ile user var mı? (Option B: otomatik merge yok)
        if (email != null && userRepository.findByPrimaryEmail(email).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, "ACCOUNT_EXISTS_DIFFERENT_SIGNIN");
        }

        // 3) User oluştur
        UserEntity user = new UserEntity();
        user.setPrimaryEmail(email);
        user.setDisplayName(name);
        userRepository.save(user);

        // 4) Identity oluştur
        UserIdentityEntity identity = new UserIdentityEntity();
        identity.setUserEntity(user);
        identity.setProvider(IdentityProvider.GOOGLE);
        identity.setProviderSubject(providerSubject);
        identity.setEmail(email);
        identity.setEmailVerified(emailVerified);
        userIdentityRepository.save(identity);

        return buildTokens(user);
    }

    @Transactional
    public AuthTokensResponse link(Authentication authentication, String idToken) {
        if (!(authentication instanceof UserIdAuthenticationToken userAuth)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
        }

        UUID userId = userAuth.getUserId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        GoogleIdToken.Payload payload = googleTokenVerifier.verify(idToken);

        String providerSubject = payload.getSubject();
        String email = payload.getEmail();
        boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());

        // Bu Google hesabı zaten başka bir user'a bağlı mı?
        if (userIdentityRepository.existsByProviderAndProviderSubject(IdentityProvider.GOOGLE, providerSubject)) {
            throw new BusinessException(HttpStatus.CONFLICT, "GOOGLE_ALREADY_LINKED");
        }

        // (Opsiyonel) Google email'i başka identity'de kullanılıyor mu?
        if (email != null && userIdentityRepository.existsByProviderAndEmail(IdentityProvider.GOOGLE, email)) {
            throw new BusinessException(HttpStatus.CONFLICT, "GOOGLE_EMAIL_ALREADY_USED");
        }

        UserIdentityEntity identity = new UserIdentityEntity();
        identity.setUserEntity(user);
        identity.setProvider(IdentityProvider.GOOGLE);
        identity.setProviderSubject(providerSubject);
        identity.setEmail(email);
        identity.setEmailVerified(emailVerified);

        userIdentityRepository.save(identity);

        // Link sonrası kullanıcıya tekrar token dönmek iyi olur (mobil için pratik)
        return buildTokens(user);
    }

    private AuthTokensResponse buildTokens(UserEntity user) {
        String access = jwtService.createAccessToken(user.getId());

        JwtService.IssuedRefreshToken issuedRefresh = jwtService.issueRefreshToken(user.getId());
        refreshTokenStore.storeActive(
                issuedRefresh.jti(),
                user.getId(),
                issuedRefresh.ttlFromNow()
        );

        return new AuthTokensResponse(
                access,
                issuedRefresh.token(),
                new AuthTokensResponse.UserInfo(user.getId(), user.getPrimaryEmail())
        );
    }
}