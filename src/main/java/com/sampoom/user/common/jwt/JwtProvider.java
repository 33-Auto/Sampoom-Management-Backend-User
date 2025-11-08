package com.sampoom.user.common.jwt;

import com.sampoom.user.common.exception.BadRequestException;
import com.sampoom.user.common.exception.CustomAuthenticationException;
import com.sampoom.user.common.exception.UnauthorizedException;
import com.sampoom.user.common.response.ErrorStatus;
import io.jsonwebtoken.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class JwtProvider {
    private final Key key;

    // Auth에서 들어오는 Key의 초기화 (캐싱용 객체)
    public JwtProvider(@Value("${jwt.secret}") String secret) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new BadRequestException(ErrorStatus.TOO_SHORT_SECRET_KEY);
        }
        this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                SignatureAlgorithm.HS256.getJcaName());
    }

    public Claims parse(String token) {
        if (token == null) {
            throw new BadRequestException(ErrorStatus.NULL_TOKEN);
        }
        if (token.isBlank()){
            throw new BadRequestException(ErrorStatus.BLANK_TOKEN);
        }
        try{
            return Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody();
        }
        catch (ExpiredJwtException e) {
            throw new CustomAuthenticationException(ErrorStatus.EXPIRED_TOKEN);
        }
        catch (Exception e) {
            // 잘못된 형식 or 위조된 토큰
            throw new CustomAuthenticationException(ErrorStatus.INVALID_TOKEN);
        }
    }

    public String resolveAccessToken(HttpServletRequest request) {
        // 쿠키에서 ACCESS_TOKEN 찾기
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("ACCESS_TOKEN".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        // Bearer 방식일 때
        String header = request.getHeader("Authorization");
        if (header == null) return null;
        if (!header.startsWith("Bearer "))
            throw new UnauthorizedException(ErrorStatus.INVALID_TOKEN);
        return header.substring(7); // "Bearer " 제거
    }
}
