package com.sampoom.backend.user.jwt;

import com.sampoom.backend.user.common.exception.BadRequestException;
import com.sampoom.backend.user.common.exception.InternalServerErrorException;
import com.sampoom.backend.user.common.response.ErrorStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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
            throw new BadRequestException(ErrorStatus.SHORT_SECRET_KEY);
        }
        this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                SignatureAlgorithm.HS256.getJcaName());
    }

    public Claims parse(String token) {
        if (token == null || token.isBlank()) {
            throw new BadRequestException(ErrorStatus.TOKEN_NULL_BLANK);
        }
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
    }
}
