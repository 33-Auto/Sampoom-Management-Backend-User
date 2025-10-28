package com.sampoom.user.common.jwt;

import com.sampoom.user.common.exception.UnauthorizedException;
import com.sampoom.user.common.response.ErrorStatus;
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

                if ("service".equals(type)) {
                    String role = claims.get("role", String.class);
                    String subject = claims.getSubject(); // 토큰 발급자 정보 (auth-service)
                    if (!"SVC_AUTH".equals(role)) {
                        throw new UnauthorizedException(ErrorStatus.TOKEN_TYPE_INVALID);
                    }

                    // Feign 내부 호출용 권한 통과
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(subject, null, List.of(() -> role));
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    // service 토큰은 더 이상 검증할 필요 없음
                    filterChain.doFilter(request, response);
                    return;
                }
                // 토큰에서 userId, role 가져오기
                String userId = claims.getSubject();
                String role = claims.get("role", String.class);
                if (userId == null|| userId.isBlank() || role == null || role.isBlank()) {
                    log.warn("토큰 필요 필드가 누락되었습니다. userId: {}, role: {}", userId, role);
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
        // Bearer 방식일 때
        return request.getHeader("Authorization");
    }
}
