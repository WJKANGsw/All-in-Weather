package com.spring.controller;

import com.spring.model.HomeUser;
import com.spring.model.UserDto;
import com.spring.model.social_dto.CustomOAuth2User;
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

    private static final Logger logger = LoggerFactory.getLogger(UserController.class); // Logger 추가

    // 사용자 등록
    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto) {
        UserDto createdUser = userService.createUser(userDto.username(), userDto.userId(), userDto.password(), userDto.email(), userDto.age());
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
    public ResponseEntity<UserDto> updateUser(@PathVariable String userId, @RequestBody UserDto userDto) {
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

    // 사용자 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        logger.info("User deleted: {}", id); // 로그 추가
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody UserDto userDto) {
        // userId를 통해 사용자 검색
        HomeUser user = userService.getUserById(userDto.userId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 비밀번호 확인
        if (!userService.checkPassword(user, userDto.password())) {
            logger.warn("Invalid password attempt for userId: {}", userDto.userId()); // 로그 추가
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid password"));
        }

        // JWT 토큰 생성
        String token = jwtTokenProvider.createToken(user.getUsername(), user.getUserId(), List.of(user.getRole().getValue()));
        logger.info("User logged in: {}", user.getUsername()); // 로그 추가

        // 사용자 정보와 토큰을 포함한 응답
        return ResponseEntity.ok(Map.of("user", Map.of("username", user.getUsername()), "token", token));
    }

    @GetMapping("/social_user")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        // 인증 객체의 내용을 로그에 출력
        System.out.println("Authentication: " + authentication);

        CustomOAuth2User user = (CustomOAuth2User) authentication.getPrincipal();
        Map<String, String> userInfo = new LinkedHashMap<>(); // 순서를 유지하는 LinkedHashMap
        userInfo.put("username", user.getUsername());
        userInfo.put("name", user.getName());
        userInfo.put("email", user.getEmail());
        userInfo.put("role", user.getRole()); // 역할 추가

        // 사용자 정보 로그 출력
        System.out.println("User Info: " + userInfo);

        return ResponseEntity.ok(userInfo);
    }
}