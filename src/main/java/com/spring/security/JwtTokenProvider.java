package com.spring.security;

import com.spring.controller.UserController;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;


import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${springboot.jwt.secret}")
    private String secretKey;
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class); // Logger 추가

    private static final long TOKEN_VALID_MILLISECOND = 1000L * 60 * 60 * 60; // 60시간
    private static final long REFRESH_TOKEN_VALID_MILLISECOND = 1000L * 60 * 60 * 24 * 7; // 1주일

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(String userId, List<String> roles) {
        return createJwtToken(userId, roles, TOKEN_VALID_MILLISECOND);
    }

    public String createRefreshToken(String userId) {
        return createJwtToken(userId, null, REFRESH_TOKEN_VALID_MILLISECOND);
    }

    private String createJwtToken(String subject, List<String> roles, long validityInMillis) {
        Date now = new Date();
        JwtBuilder jwtBuilder = Jwts.builder()
            .setSubject(subject)
            .issuedAt(now)
            .setExpiration(new Date(now.getTime() + validityInMillis))
            .signWith(getSigningKey());

        if (roles != null) {
            jwtBuilder.claim("roles", roles);
        }

        return jwtBuilder.compact();
    }

    public long getTokenValidMillisecond() {
        return TOKEN_VALID_MILLISECOND;
    }

    public UsernamePasswordAuthenticationToken getAuthentication(String token) {
        String userId = getUserId(token);
        return new UsernamePasswordAuthenticationToken(userId, null, null);
    }

    public String getUserId(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
        return claims.getSubject();
    }

    public String resolveToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        logger.debug("Authorization 헤더 값: {}", token);
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
