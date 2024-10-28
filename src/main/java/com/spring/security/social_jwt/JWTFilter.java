package com.spring.security.social_jwt;

import com.spring.model.social_dto.CustomOAuth2User;
import com.spring.model.social_dto.SocialUserDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {

        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {  // 쿠키가 있을 경우만 루프를 돈다
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("Authorization")) {
                    authorization = cookie.getValue();
                }
            }
        }

        // Authorization 헤더 검증
        if (authorization == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰
        String token = authorization;

        // Claims에서 loginType 가져오기
        Claims claims = Jwts.parser()
            .verifyWith(jwtUtil.getSecretKey())
            .build()
            .parseSignedClaims(token)
            .getBody();

        String loginType = claims.get("loginType", String.class);

        // 소셜 로그인 처리
        if ("social".equals(loginType)) {
            if (jwtUtil.isExpired(token)) {
                System.out.println("소셜 로그인 토큰 만료됨");
                filterChain.doFilter(request, response);
                return;
            }

            // 유효한 토큰이라면, 인증 정보 설정
            String username = jwtUtil.getUsername(token);
            String name = jwtUtil.getName(token);
            String email = jwtUtil.getEmail(token);
            String role = jwtUtil.getRole(token);

            // 사용자 정보로 DTO 생성
            SocialUserDTO sc_userDTO = new SocialUserDTO();
            sc_userDTO.setUsername(username);
            sc_userDTO.setName(name);
            sc_userDTO.setEmail(email);
            sc_userDTO.setRole(role);

            // 사용자 인증 객체 생성
            CustomOAuth2User customOAuth2User = new CustomOAuth2User(sc_userDTO);

            // 스프링 시큐리티 인증 토큰 생성 및 설정
            Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);
            System.out.println("소셜 로그인 인증 완료: " + username);
        }

        filterChain.doFilter(request, response);
    }
}