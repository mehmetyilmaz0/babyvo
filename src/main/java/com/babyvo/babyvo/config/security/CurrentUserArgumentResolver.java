package com.babyvo.babyvo.config.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.babyvo.babyvo.common.exception.BusinessException;

import java.util.UUID;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && UUID.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
        }

        // JwtAuthenticationFilter senin custom token’ını koyuyor olmalı
        // UserIdAuthenticationToken kullanıyorsun → principal/credentials’dan userId çekiyoruz
        if (auth instanceof UserIdAuthenticationToken t) {
            UUID userId = t.getUserId();
            if (userId == null) {
                throw new BusinessException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
            }
            return userId;
        }

        // Eğer ileride başka auth tipleri gelirse:
        Object principal = auth.getPrincipal();
        if (principal instanceof UUID uuid) {
            return uuid;
        }
        if (principal instanceof String s) {
            try {
                return UUID.fromString(s);
            } catch (Exception ignored) {}
        }

        throw new BusinessException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }
}