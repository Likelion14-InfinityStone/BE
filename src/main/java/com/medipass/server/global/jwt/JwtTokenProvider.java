package com.medipass.server.global.jwt;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final SecretKey key;

    @Value("${jwt.access.expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpiration;

    private String createToken(String userId, long validity) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + validity))
                .signWith(key)
                .compact();
    }

    public String createAccessToken(String userId)  { return createToken(userId, accessExpiration); }
    public String createRefreshToken(String userId) { return createToken(userId, refreshExpiration); }
}
