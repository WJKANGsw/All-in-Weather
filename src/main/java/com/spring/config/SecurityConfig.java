package com.spring.config;

import com.spring.oauth2.CustomSuccessHandler;
import com.spring.security.JwtAuthenticationFilter;
import com.spring.security.social_jwt.JWTFilter;
import com.spring.security.social_jwt.JWTUtil;
import com.spring.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final JWTUtil jwtUtil;
  private final CustomOAuth2UserService customOAuth2UserService;
  private final CustomSuccessHandler customSuccessHandler;

  private static final List<String> ALLOWED_ORIGIN_PATTERNS = Arrays.asList(
      "http://localhost:*",       // 개발 환경
      "http://localhost:8080",       // 테스트 환경
      "http://14.63.178.32",       // 배포 환경
      "https://allinweather.site"       // 배포 환경

  );

  private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
      "/api/users/**",
      "/api/test/**",
      "/api/schedules/**",
      "/api/chat/**",
      "/api/notifications/**",
      "/api/upload-dalle-image",
      "/",
      "/api/location",
      "/actuator/health" // 헬스 체크 엔드포인트
  );

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers(PUBLIC_ENDPOINTS.toArray(String[]::new)).permitAll()
            .anyRequest().authenticated()
        )
        .exceptionHandling(exceptionHandling -> exceptionHandling
            .authenticationEntryPoint((request, response, authException) ->
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied")
            )
        )
        .addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .oauth2Login(oauth2 -> oauth2
          .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
              .userService(customOAuth2UserService))
          .successHandler(customSuccessHandler)
        );

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    configuration.setAllowedOriginPatterns(ALLOWED_ORIGIN_PATTERNS); // 명시적으로 허용할 Origin 패턴
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Collections.singletonList("*"));
    configuration.setExposedHeaders(Arrays.asList("Authorization", "Set-Cookie"));
    configuration.setAllowCredentials(true); // 자격 증명 허용
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
