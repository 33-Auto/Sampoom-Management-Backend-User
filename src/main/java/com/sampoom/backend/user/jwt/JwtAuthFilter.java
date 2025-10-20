package com.sampoom.backend.user.jwt;

import com.sampoom.backend.user.common.exception.UnauthorizedException;
import com.sampoom.backend.user.common.response.ErrorStatus;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String accessToken = resolveAccessToken(request);

        if (accessToken != null) {
            if (accessToken.startsWith("Bearer ")) {
                accessToken = accessToken.substring(7);
            }
            try {
                Claims claims = jwtProvider.parse(accessToken);
                // 토큰 타입 검증
                String type = claims.get("type", String.class);
                if ("refresh".equals(type)) {
                    SecurityContextHolder.clearContext(); // 인증 정보 제거
                    throw new UnauthorizedException(ErrorStatus.TOKEN_TYPE_INVALID);
                }
                // 토큰에서 userId, role 가져오기
                String userId = claims.getSubject();
                String role = claims.get("role", String.class);
                if (userId == null|| userId.isBlank() || role == null || role.isBlank()) {
                    filterChain.doFilter(request, response);
                    return;
                }
                // Spring Security는 ROLE_ 접두사를 기대함
                // 접두사가 없으면 붙여주고, 있으면 그대로 둔다.
                String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, List.of(() -> authority)
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                // 토큰 검증 실패 시 SecurityContext 비움
                SecurityContextHolder.clearContext();
                throw e;
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveAccessToken(HttpServletRequest request) {
        // 쿠키에서 ACCESS_TOKEN 찾기
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("ACCESS_TOKEN".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // Swagger / 테스트용 헤더 지원
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
