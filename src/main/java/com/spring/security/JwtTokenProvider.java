package com.spring.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private final String SECRET_KEY = "your_secret_key"; // 비밀 키
    private final long VALIDITY = 604800000L; // 7일 (밀리초)

    // JWT 토큰 생성
    public String generateToken(Authentication authentication) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", authentication.getName());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + VALIDITY))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    // JWT 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // JWT 토큰으로부터 인증 정보 얻기
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();

        String username = claims.get("username", String.class);
        // 여기에서 UserDetailsService를 통해 사용자 정보를 로드하고 인증 객체를 생성할 수 있습니다.
        // UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return null; // 실제로는 Authentication 객체를 반환해야 합니다.
    }
}