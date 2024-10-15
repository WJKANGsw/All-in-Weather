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
@AllArgsConstructor
@Builder
@Table(name = "homeuser")
public class HomeUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;


    @Column(nullable = false, length = 255)
    @JsonIgnore // JSON 응답에서 비밀번호를 숨김
    private String password; // 비밀번호는 해시된 값으로 저장해야 합니다.

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // 추가 생성자
    public HomeUser(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role; // 사용자 역할 필드 추가

    // 추가 필드 예시
    // private String firstName;
    // private String lastName;
}
