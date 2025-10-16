package com.sampoom.backend.user.jwt;

import com.sampoom.backend.user.common.exception.UnauthorizedException;
import com.sampoom.backend.user.common.response.ErrorStatus;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                Claims claims = jwtProvider.parse(token);
                // 토큰 타입 검증
                String type = claims.get("type", String.class);
                if ("refresh".equals(type)) {
                    SecurityContextHolder.clearContext(); // 인증 정보 제거
                    throw new UnauthorizedException(ErrorStatus.TOKEN_TYPE_INVALID);
                }
                // 토큰에서 userId, role 가져오기
                String userId = claims.getSubject();
                String role = claims.get("role", String.class);
                if (userId == null || role == null) {
                    log.warn("토큰에 필수 정보가 누락되었습니다. userId: {}, role: {}", userId, role);
                    chain.doFilter(request, response);
                    return;
                }
                // ROLE 누락되면 달아주기
                String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;

                var authToken = new UsernamePasswordAuthenticationToken(
                        userId, null,  List.of(() -> authority));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception e) {
                // 토큰 검증 실패 시 SecurityContext 비움
                SecurityContextHolder.clearContext();
                throw e;
            }
        }
        chain.doFilter(request, response);
    }
}
