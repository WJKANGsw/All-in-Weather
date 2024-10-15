package com.spring.model;

//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//public class UserDto {
//    private Long id;            // 사용자 ID
//    private String username;    // 사용자 이름
//    private String email; // 사용자 이메일
//}

public record UserDto(Long id, String username, String email, String password) {
    // id, username, email, password 필드
}