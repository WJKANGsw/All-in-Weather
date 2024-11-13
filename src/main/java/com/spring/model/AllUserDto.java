package com.spring.model;

//public record AllUserDto (
//    String userId,       // 프론트에서 보내는 userId
//    String password,     // 프론트에서 보내는 password
//    String email         // 프론트에서 보내는 email
//) { }
//

import lombok.*;

@Getter
@Setter
public class AllUserDto {
  private String userId;
  private String password; // 소셜 로그인 시에는 null
  private String email;
  private String nickname; // 소셜 로그인 시 추가적으로 필요한 필드
  private String name;
  private String role;
  private Integer age;
  private String gender;
  private Double height;
  private Double weight;
  private boolean profileComplete; // 소셜 로그인 추가 정보 입력 여부

  public AllUserDto() {
  }

  // 3개의 인자를 받는 생성자 (일반 로그인)
  public AllUserDto(String userId, String password, String email) {
    this.userId = userId;
    this.password = password;
    this.email = email;
  }

  // 5개의 인자를 받는 생성자 (소셜 로그인)
  public AllUserDto(String userId, String password, String email, String nickname, boolean profileComplete) {
    this.userId = userId;
    this.password = password;
    this.email = email;
    this.nickname = nickname;
    this.profileComplete = profileComplete;
  }

  // AllUser를 받아서 필드를 설정하는 생성자 추가
  public AllUserDto(AllUser user) {
    this.userId = user.getUserId();
    this.password = user.getPassword();  // 소셜 로그인일 경우 null일 수 있음
    this.email = user.getEmail();
    this.nickname = user.getNickname();
    this.name = user.getName();
    this.role = user.getRole() != null ? user.getRole().name() : null;
    this.age = user.getAge();
    this.gender = user.getGender();
    this.height = user.getHeight();
    this.weight = user.getWeight();
  }


}


