package com.sampoom.backend.user.security;

import com.sampoom.backend.user.jwt.JwtProvider;
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
                var authToken = new UsernamePasswordAuthenticationToken(
                        claims.getSubject(), null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception ignored) {
                // 유효하지 않은 토큰이면 인증 미설정 → 컨트롤러에서 401
            }
        }
        chain.doFilter(req, res);
    }
}
