package com.spring.model;

import jakarta.persistence.*;
import lombok.Data;

import java.security.AuthProvider;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "alluser")
public class AllUser {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String userId; // 일반 로그인 ID 또는 소셜 로그인 ID (e.g., google12345)

  private String password; // 소셜 로그인 사용자는 NULL

  @Column(unique = true)
  private String email;

  @Enumerated(EnumType.STRING)
  private LoginType provider; // 로그인 방법 (e.g., 일반, GOOGLE, KAKAO, NAVER)

  private String nickname; // 랜덤 닉네임 생성

  @Enumerated(EnumType.STRING) // EnumType.STRING으로 설정하여 ROLE_USER, ROLE_ADMIN 등을 문자열로 저장
  private UserRole role; // 사용자 권한 (e.g., ROLE_USER, ROLE_ADMIN)

  private String name; // 실명 (소셜로그인에서만 가져옴, 일반로그인인 경우 선택적 or X)

  @Column(nullable = true)
  private Integer age; // 나이 (nullable로 변경)

  private String gender;

  @Column(nullable = true)
  private Double height;

  @Column(nullable = true)
  private Double weight;

  private LocalDate registrationDate; // 회원가입 날짜
}
