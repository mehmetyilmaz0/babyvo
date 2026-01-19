package com.babyvo.babyvo.config.security;

import com.babyvo.babyvo.service.auth.jwt.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring("Bearer ".length()).trim();
            try {
                UUID userId = jwtService.parseAccessTokenAndGetUserId(token);
                SecurityContextHolder.getContext().setAuthentication(new UserIdAuthenticationToken(userId));
            } catch (Exception ignored) {
                // Token invalidse context boş kalsın; SecurityConfig 401 döndürür
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}