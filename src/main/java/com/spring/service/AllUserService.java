package com.spring.service;

import com.spring.model.*;
import com.spring.repository.AllUserRepository;
import com.spring.repository.UserStyleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AllUserService {

  private final AllUserRepository userRepository;
  private final UserStyleRepository userStyleRepository;
  private final PasswordEncoder passwordEncoder; // BCryptPasswordEncoder 주입
  private static final Logger logger = LoggerFactory.getLogger(AllUserService.class);

  // alluser 일반로그인 방식 회원가입
  @Transactional
  public AllUser registerUser(AllUserDto userDto) {
    // 비밀번호 해시화 (일반 로그인 시)
    String hashedPassword = hashPassword(userDto.getPassword());

    // 새로운 사용자 객체 생성 및 설정
    AllUser user = new AllUser();
    user.setUserId(userDto.getUserId()); // 수정된 DTO 접근 방식
    user.setPassword(hashedPassword); // 소셜 로그인 시에는 null일 수 있음
    user.setEmail(userDto.getEmail());
    user.setProvider(LoginType.general); // 일반 로그인으로 설정
    user.setNickname(generateRandomNickname());
    user.setRole(UserRole.USER);

    // 나이, 성별, 키, 몸무게는 기본적으로 null로 설정
    user.setAge(null);
    user.setGender(null);
    user.setHeight(null);
    user.setWeight(null);
    user.setName(null);

    // 회원가입 날짜 설정
    user.setRegistrationDate(LocalDate.now());

    // 사용자 정보 저장 후 반환
    return userRepository.save(user);
  }

  // 일반로그인 사용자 랜덤 닉네임 설정
  private String generateRandomNickname() {
    String[] adjectives = {"행복한", "멋진", "빛나는", "용감한", "지혜로운"};
    String[] nouns = {"사자", "호랑이", "독수리", "상어", "불사조"};
    return adjectives[(int) (Math.random() * adjectives.length)]
        + nouns[(int) (Math.random() * nouns.length)]
        + (int) (Math.random() * 1000);
  }

  // 사용자 비밀번호 해시화
  private String hashPassword(String password) {
    return new BCryptPasswordEncoder().encode(password);
  }


  public Optional<AllUser> getUserByUserId(String userId) {
    return userRepository.findByUserId(userId); // userId로 사용자 찾기
  }

  public boolean checkPassword(AllUser user, String rawPassword) {
    return passwordEncoder.matches(rawPassword, user.getPassword());
  }

  // 회원가입 후 추가정보 받아서 업데이트
  public AllUser updateUserInfo(String userId, AllUserDto userDto) throws UserNotFoundException {
    // userId로 사용자를 조회
    Optional<AllUser> optionalUser = userRepository.findByUserId(userId);

    // 사용자가 없으면 예외 처리
    if (!optionalUser.isPresent()) {
      logger.error("User with userId {} not found", userId);
    }

    // 사용자 객체 가져오기
    AllUser user = optionalUser.get();

    // 요청 본문에 있는 값으로 사용자 정보 업데이트
    user.setAge(userDto.getAge());
    user.setGender(userDto.getGender());
    user.setHeight(userDto.getHeight());
    user.setWeight(userDto.getWeight());

    // 업데이트된 사용자 정보 저장
    return userRepository.save(user);
  }


  // 사용자가 선택한 스타일 목록 저장
  public void saveUserStyles(String userId, List<String> styles) {
    AllUser user = userRepository.findByUserId(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    for (String style : styles) {
      UserStyle userStyle = new UserStyle();
      userStyle.setUserId(user);
      userStyle.setStyle(style);
      userStyleRepository.save(userStyle);
    }
  }
}
