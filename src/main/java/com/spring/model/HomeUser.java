package com.spring.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "homeuser")
public class HomeUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 50)
    private String userId;

    @Column(nullable = false, length = 255)
    @JsonIgnore
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // 나이, 성별 ... 취향?
    @Column(nullable = false)
    private Integer age; // 나이 필드 추가

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    // 모든 필드를 받는 생성자
    public HomeUser(Long id, String username, String userId, String password, String email, UserRole role, Integer age) {
        this.id = id;
        this.username = username;
        this.userId = userId;
        this.password = password;
        this.email = email;
        this.role = role;
        this.age = age; // 추가된 부분
    }

    // 비밀번호와 역할이 없는 생성자
    public HomeUser(String userId, String username, String password, String email, Integer age) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.age = age; // 추가된 부분
        this.role = UserRole.USER; // 기본 역할 설정
    }
}
