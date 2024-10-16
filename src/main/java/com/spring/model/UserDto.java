package com.spring.model;

public record UserDto(Long id, String username, String userId, String email, String password) {
    // id, username, email, password 필드
}