package com.sampoom.backend.user.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                Claims claims = jwtProvider.parse(token);
                // 토큰에서 userId, role 가져오기
                String userId = claims.getSubject();
                String role = claims.get("role", String.class);
                // ROLE 누락되면 달아주기
                String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;

                var authToken = new UsernamePasswordAuthenticationToken(
                        userId, null,  List.of(() -> authority));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception ignored) {
                // 유효하지 않은 토큰이면 인증 미설정 → 컨트롤러에서 401
            }
        }
        chain.doFilter(req, res);
    }
}
