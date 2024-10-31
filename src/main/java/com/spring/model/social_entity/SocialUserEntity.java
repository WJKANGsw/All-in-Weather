package com.spring.model.social_entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SocialUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username; // google + providerId
    private String name; // 이름

    private String email; // 이메일 주소

    private String role; // ROLE_USER

    private String nickname; // random Nickname
}
