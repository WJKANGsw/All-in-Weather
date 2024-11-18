package com.spring.service;

import com.spring.model.*;
import com.spring.repository.AllUserRepository;
import com.spring.repository.UserStyleRepository;
import com.spring.repository.social.RecommendationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AllUserService {

  private final AllUserRepository userRepository;
  private final UserStyleRepository userStyleRepository;
  private final PasswordEncoder passwordEncoder; // BCryptPasswordEncoder 주입
  private final RecommendationRepository recommendationRepository;
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

  // 일반로그인 아이디 변경
  public AllUserDto updateUserId(String userId){
    Optional<AllUser> userOptional = userRepository.findByUserId(userId);
    if (userOptional.isPresent()) {
      AllUser user = userOptional.get();
      user.setUserId(userId);

      userRepository.save(user);
      return new AllUserDto(user.getUserId(), user.getNickname(),user.getEmail(),user.getAge(),
          user.getGender(),user.getHeight(),user.getWeight());}
    return null;
  }

  // 사용자 비밀번호 해시화
  private String hashPassword(String password) {
    return new BCryptPasswordEncoder().encode(password);
  }

  // 비밀번호 업데이트
  @Transactional
  public void updatePassword(String userId, String newPassword) {
    AllUser user = userRepository.findByUserId(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found"));
    user.setPassword(hashPassword(newPassword)); // 비밀번호 해시화
  }


  public boolean checkPassword(AllUser user, String rawPassword) {
    return passwordEncoder.matches(rawPassword, user.getPassword());
  }

  // allUser 일반로그인 사용자 탈퇴시 비밀번호 검증
  public boolean verifypassword(String userId, String rawPassword) {
    AllUser user = userRepository.findByUserId(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found"));
    return passwordEncoder.matches(rawPassword, user.getPassword());
  }

  public Optional<AllUser> getUserByUserId(String userId) {
    return userRepository.findByUserId(userId); // userId로 사용자 찾기
  }

  @Transactional
  public void deleteUser(String userId) {
    userStyleRepository.deleteByUserId_UserId(userId);
    recommendationRepository.deleteByUserId_UserId(userId);
    Optional<AllUser> user = userRepository.findByUserId(userId);
    if (user.isPresent()) {
      userRepository.delete(user.get()); // Optional에서 get()을 사용하여 엔티티를 꺼냄
      logger.info("Deleted social user with username: {}", userId);
    } else {
      throw new UsernameNotFoundException("Social user not found");
    }
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

  // 사용자 스타일 가져오기
  public List<UserStyle> getUserStyle(String userId) {
    List<UserStyle> userStyles = userStyleRepository.findByUserId_UserId(userId);
    if (!userStyles.isEmpty()) {
      return userStyles;
    } else {
      return null; // 스타일이 없는 경우
    }
  }
}