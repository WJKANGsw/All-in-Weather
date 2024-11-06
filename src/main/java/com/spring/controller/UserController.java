package com.spring.controller;

import com.spring.model.HomeUser;
import com.spring.model.UserDto;
import com.spring.model.dto.request.auth.CheckCertificationRequestDto;
import com.spring.model.dto.request.auth.EmailCertificationRequestDto;
import com.spring.model.dto.request.auth.IdCheckRequestDto;
import com.spring.model.social_dto.CustomOAuth2User;
import com.spring.model.social_dto.SocialUserDTO;
import com.spring.model.social_entity.SocialUserEntity;
import com.spring.repository.social.SocialUserRepository;
import com.spring.security.JwtTokenProvider;
import com.spring.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;
    private final SocialUserRepository socialUserRepository;


    private static final Logger logger = LoggerFactory.getLogger(UserController.class); // Logger 추가


    @PostMapping("/check-userId")
    public ResponseEntity<Boolean> checkUserId(@RequestBody IdCheckRequestDto requestDto) {
        boolean exists = userService.userIdExists(requestDto.getUserId());
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @PostMapping("/send-verification-code")
    public ResponseEntity<?> sendVerificationCode(@RequestBody EmailCertificationRequestDto requestDto) {
        boolean isSent = userService.sendEmailCertification(requestDto.getId(), requestDto.getEmail());

        if (!isSent) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ID가 이미 존재하거나 메일 전송 실패");
        }

        return ResponseEntity.ok("인증 코드가 발송되었습니다.");
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody CheckCertificationRequestDto requestDto) {
        boolean isVerified = userService.verifyCertificationCode(
            requestDto.getId(), requestDto.getEmail(), requestDto.getCertificationNumber()
        );

        if (!isVerified) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증 코드가 일치하지 않습니다.");
        }

        return ResponseEntity.ok("인증이 완료되었습니다.");
    }


    // 사용자 등록
    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto) {
        logger.info("Registering user: {}", userDto.username()); // 등록 시작 로그

        // 사용자 등록
        UserDto createdUser = userService.createUser(userDto.username(), userDto.userId(), userDto.password(), userDto.email(), userDto.age());

        logger.info("User registered successfully: {}", createdUser.username()); // 성공적인 등록 로그
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }


    // 사용자 조회
    @GetMapping("/show/{userId}")
    public ResponseEntity<HomeUser> getUser(@PathVariable String userId) {
        return userService.getUserById(userId)
                .map(userDto -> {
                    logger.info("User retrieved: {}", userDto.getUserId()); // 로그 추가
                    return ResponseEntity.ok(userDto);
                })
                .orElseGet(() -> {
                    logger.warn("User not found: {}", userId); // 로그 추가
                    return ResponseEntity.notFound().build();
                });
    }

    // 사용자 업데이트
    @PutMapping("/update/{userId}")
    public ResponseEntity<UserDto> updateUser(@RequestBody UserDto userDto) {
        UserDto updatedUser = userService.updateUser(userDto.id(), userDto.userId(), userDto.username(), userDto.email(), userDto.password(), userDto.age());
        return ResponseEntity.ok(updatedUser);
    }

    // 비밀번호 업데이트
    @PutMapping("/password/{userId}")
    public ResponseEntity<Void> updatePassword(@PathVariable String userId, @RequestBody Map<String, String> requestBody) {
        String password = requestBody.get("password");
        userService.updatePassword(userId, password);
        UserDto updatedUser = userService.updateUserId(userId); // 새로운 userId로 업데이트
        logger.info("Password updated for user: {}", password);
        logger.info("UserId updated for user: {}", userId);
        return ResponseEntity.noContent().build();
    }

    // 일반로그인 탈퇴시 비밀번호 검증
    @PostMapping("/verify-password")
    public ResponseEntity<Void> verifyPassword(@RequestBody Map<String, String> request){
        String userId = request.get("userId");
        String password = request.get("password");


        boolean isValid = userService.verifypassword(userId, password);
        if (isValid){
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody UserDto userDto) {
        logger.info("Login attempt for userId: {}", userDto.userId()); // 로그인 시도 로그

        // userId를 통해 사용자 검색
        HomeUser user = userService.getUserById(userDto.userId())
            .orElseThrow(() -> {
                logger.warn("User not found: {}", userDto.userId()); // 사용자 미발견 로그
                return new UsernameNotFoundException("User not found");
            });

        // 비밀번호 확인
        if (!userService.checkPassword(user, userDto.password())) {
            logger.warn("Invalid password attempt for userId: {}", userDto.userId()); // 잘못된 비밀번호 로그
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid password"));
        }

        // JWT 토큰 생성
        String token = jwtTokenProvider.createToken(user.getUsername(), user.getUserId(), List.of(user.getRole().getValue()));

        logger.info("User logged in successfully: {}", user.getUsername()); // 성공적인 로그인 로그

        // 사용자 정보와 토큰을 포함한 응답
        return ResponseEntity.ok(Map.of("user", Map.of("username", user.getUsername()), "token", token));
    }


    @GetMapping("/social_user")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        // 사용자 이름을 통해 사용자 정보를 다시 가져옵니다.
        CustomOAuth2User user = (CustomOAuth2User) authentication.getPrincipal();
        SocialUserEntity socialUserEntity = socialUserRepository.findByUsername(user.getUsername());

        // 최신 사용자 정보로 갱신
        user.setNickname(socialUserEntity.getNickname());
        user.setName(socialUserEntity.getName());
        user.setEmail(socialUserEntity.getEmail());

        Map<String, String> userInfo = new LinkedHashMap<>(); // 순서를 유지하는 LinkedHashMap
        userInfo.put("social_username", user.getUsername());
        userInfo.put("name", user.getName());
        userInfo.put("email", user.getEmail());
        userInfo.put("role", user.getRole());
        userInfo.put("nickname", user.getNickname()); // 닉네임 추가

        // 사용자 정보 로그 출력
        System.out.println("User Info: " + userInfo);
        return ResponseEntity.ok(userInfo);
    }


    @PutMapping("/update/social/{username}")
    public ResponseEntity<?> updateSocialUser(
        @PathVariable String username,
        @RequestBody SocialUserDTO socialUserDTO) {
        try {
            // 사용자 정보를 수정하는 서비스 호출
            SocialUserDTO updatedUser = userService.updateSocialUser(username, socialUserDTO);
            return ResponseEntity.ok(updatedUser); // 성공적으로 수정된 사용자 정보 반환
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("회원 정보 수정 중 오류가 발생했습니다."); // 오류 메시지 반환
        }
    }

    // 소셜 사용자 탈퇴
    @DeleteMapping("delete/social_user/{username}")
    public ResponseEntity<Void> deleteSocialUser(@PathVariable String username) {
        try {
            userService.deleteSocialUser(username);
            logger.info("Social user deleted: {}", username); // 로그 추가
            return ResponseEntity.noContent().build(); // 성공시 204 No Content 반환
        } catch (Exception e) {
            logger.error("Error deleting social user: {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 오류시 500 에러 반환
        }
    }

}