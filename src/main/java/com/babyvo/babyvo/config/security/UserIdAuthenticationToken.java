package com.babyvo.babyvo.config.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.UUID;

public class UserIdAuthenticationToken extends AbstractAuthenticationToken {

    private final UUID userId;

    public UserIdAuthenticationToken(UUID userId) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.userId = userId;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }

    public UUID getUserId() {
        return userId;
    }
}