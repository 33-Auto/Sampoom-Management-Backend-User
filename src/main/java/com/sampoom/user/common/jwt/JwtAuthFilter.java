package com.sampoom.user.common.jwt;

import com.sampoom.user.common.entity.Role;
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

import static com.sampoom.user.common.entity.Role.ADMIN;
import static com.sampoom.user.common.entity.Role.MEMBER;

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

                // service 토큰 검증
                if ("service".equals(type)) {
                    log.info("[Signup] service 토큰 검증 진입");
                    String role = claims.get("role", String.class);
                    String subject = claims.getSubject(); // 토큰 발급자 정보 (auth-service)
                    if (role == null || role.isBlank()) {
                        log.warn("서비스 토큰 필수 필드 누락. subject: {}, role: {}", subject, role);
                        throw new UnauthorizedException(ErrorStatus.TOKEN_TYPE_INVALID);
                        }
                    if (!role.startsWith("SVC_")) {
                        throw new UnauthorizedException(ErrorStatus.TOKEN_TYPE_INVALID);
                    }

                    // Feign 내부 호출용 권한 통과
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(subject, null, List.of(() -> role));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.info("[Signup] feign 권한 통과");
                    // service 토큰은 더 이상 검증할 필요 없음
                    filterChain.doFilter(request, response);
                    return;
                }

                // 토큰에서 userId, role 가져오기
                String userId = claims.getSubject();
                String roleClaim = claims.get("role", String.class);
                if (userId == null || userId.isBlank() || roleClaim == null || roleClaim.isBlank()) {
                    log.warn("토큰 필요 필드가 누락되었습니다. userId: {}, role: {}", userId, roleClaim);
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }
                Role role;
                    try {
                        role = Role.valueOf(roleClaim);
                    } catch (IllegalArgumentException ex) {
                        throw new UnauthorizedException(ErrorStatus.TOKEN_TYPE_INVALID);
                    }

                // 권한 매핑 (Enum Role → Security 권한명)
                String authority;
                switch (role) {
                    case MEMBER -> authority = "ROLE_USER";
                    case ADMIN -> authority = "ROLE_ADMIN";
                    default -> authority = "ROLE_" + role.name();
                }

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
