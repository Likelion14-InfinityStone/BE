package com.medipass.server.global.jwt;

import com.medipass.server.global.jwt.exception.TokenExpiredException;
import com.medipass.server.global.jwt.exception.TokenInvalidException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class JwtTokenUtil {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtParser parser;

    // JWT를 서명 검증과 함께 파싱하여 Claims를 반환
    public Claims parseClaims(String token) {
        return parser.parseSignedClaims(token).getPayload();
    }

    // 토큰의 subject(userId) 추출
    public String getUserId(String token) {
        return parseClaims(token).getSubject();
    }

    // 토큰 유효성(서명, 만료, 형식)을 검사 (예외를 던짐)
    public void validateTokenThrows(String token) {
        try {
            parseClaims(token); // 예외 발생

        } catch (ExpiredJwtException e) { // 토큰 만료
            throw new TokenExpiredException();
        } catch (JwtException | IllegalArgumentException e) {
            throw new TokenInvalidException();
        }
    }

    // Authorization 헤더에서 Bearer 토큰 문자열을 추출
    public String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(header) || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return header.substring(BEARER_PREFIX.length());
    }
}
