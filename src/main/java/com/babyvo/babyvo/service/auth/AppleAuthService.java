package com.babyvo.babyvo.service.auth;

import com.babyvo.babyvo.common.exception.BusinessException;
import com.babyvo.babyvo.entity.enums.IdentityProvider;
import com.babyvo.babyvo.entity.user.UserEntity;
import com.babyvo.babyvo.entity.user.UserIdentityEntity;
import com.babyvo.babyvo.repository.user.UserIdentityRepository;
import com.babyvo.babyvo.repository.user.UserRepository;
import com.babyvo.babyvo.response.auth.AuthTokensResponse;
import com.babyvo.babyvo.service.auth.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppleAuthService {

    private final AppleTokenVerifier appleTokenVerifier;
    private final UserRepository userRepository;
    private final UserIdentityRepository userIdentityRepository;
    private final JwtService jwtService;
    private final RefreshTokenStore refreshTokenStore;

    @Transactional
    public AuthTokensResponse login(String idToken) {
        var verified = appleTokenVerifier.verify(idToken);

        // 1) Identity üzerinden user bul
        UserIdentityEntity identity = userIdentityRepository
                .findByProviderAndProviderSubject(IdentityProvider.APPLE, verified.subject())
                .orElseGet(() -> {
                    // yeni user + identity
                    UserEntity user = new UserEntity();
                    // Apple email her login’de gelmeyebilir. İlk login’de gelirse primaryEmail setle.
                    if (verified.email() != null && !verified.email().isBlank()) {
                        user.setPrimaryEmail(verified.email());
                    }
                    user = userRepository.save(user);

                    UserIdentityEntity ui = new UserIdentityEntity();
                    ui.setUserEntity(user);
                    ui.setProvider(IdentityProvider.APPLE);
                    ui.setProviderSubject(verified.subject());
                    ui.setEmail(verified.email());
                    ui.setEmailVerified(verified.emailVerified());
                    return userIdentityRepository.save(ui);
                });

        UserEntity user = identity.getUserEntity();

        // Eğer user.primaryEmail boş ama identity’de email var ise doldur (optional)
        if ((user.getPrimaryEmail() == null || user.getPrimaryEmail().isBlank())
                && identity.getEmail() != null && !identity.getEmail().isBlank()) {
            user.setPrimaryEmail(identity.getEmail());
            user = userRepository.save(user);
        }

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "USER_DELETED");
        }

        // 2) JWT üret
        String access = jwtService.createAccessToken(user.getId());

        // Burada senin JwtService’te refresh üretimini nasıl yaptıysak ona uyalım:
        // Eğer "issueRefreshToken" (token + jti + ttl) metodu sende varsa onu kullan.
        var refresh = jwtService.issueRefreshToken(user.getId());

        // 3) Redis'e store
        refreshTokenStore.storeActive(refresh.jti(), user.getId(), refresh.ttlFromNow());

        return new AuthTokensResponse(
                access,
                refresh.token(),
                new AuthTokensResponse.UserInfo(user.getId(), user.getPrimaryEmail())
        );
    }
}