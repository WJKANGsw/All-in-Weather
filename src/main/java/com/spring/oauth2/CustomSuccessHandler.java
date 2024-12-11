package com.spring.oauth2;

import com.spring.model.social_dto.CustomOAuth2User;
import com.spring.security.social_jwt.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;

    public CustomSuccessHandler(JWTUtil jwtUtil) {

        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        //OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        String name = customUserDetails.getName(); // 실명
        String userId = customUserDetails.getUsername(); // google11020133301
        String email = customUserDetails.getEmail(); // 이메일
        String nickname = customUserDetails.getNickname(); // 랜덤닉네임

        boolean profileComplete = customUserDetails.isProfileComplete();

        System.out.println("Profile complete status in success handler: " + profileComplete);  // 로그 추가


        if (userId == null || userId.isEmpty() || email == null || email.isEmpty() || name == null || name.isEmpty() || nickname == null || nickname.isEmpty()) {
            System.out.println("Invalid user details: " + userId + ", " + name + ", " + email + ", " + nickname);
            throw new IllegalArgumentException("User details are not properly set. Please check the user details.");
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String token = jwtUtil.createJwt(userId, role, name, email, nickname ,60 * 60 * 60 * 60L, "social",profileComplete);

        response.addCookie(createCookie("Authorization", token));
        if (profileComplete) {
            // 프로필이 완료된 경우 메인 페이지로 리다이렉트
            response.sendRedirect("http://localhost:5173/dashboard");
            //response.sendRedirect("https://allinweather.site/dashboard");
        } else {
            // 추가 정보가 필요한 경우 사용자 정보 입력 페이지로 리다이렉트
            response.sendRedirect("http://localhost:5173/addUserInfo");
            //response.sendRedirect("https://allinweather.site/addUserInfo");
        }
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60*60*60*60);
        cookie.setPath("/");
        //cookie.setHttpOnly(true);

        return cookie;
    }
}
