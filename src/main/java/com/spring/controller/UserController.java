package com.spring.controller;

import com.spring.model.HomeUser;
import com.spring.model.UserDto;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        UserDto createdUser = userService.createUser(userDto.username(), userDto.userId(), userDto.password(), userDto.email());
        logger.info("User registered: {}", createdUser.username()); // 로그 추가
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    // 사용자 조회
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        return userService.getUser(id)
                .map(userDto -> {
                    logger.info("User retrieved: {}", userDto.username()); // 로그 추가
                    return ResponseEntity.ok(userDto);
                })
                .orElseGet(() -> {
                    logger.warn("User not found: {}", id); // 로그 추가
                    return ResponseEntity.notFound().build();
                });
    }

    // 사용자 업데이트
    @PutMapping("/update")
    public ResponseEntity<UserDto> updateUser(@RequestBody UserDto userDto) {
        // JWT에서 userId 가져오기
        String userId = jwtTokenProvider.getUserIdFromToken(); // JWT에서 사용자 ID를 가져옴

        // 유효성 검사
        if (userDto.username() == null || userDto.username().isEmpty() ||
            userDto.email() == null || userDto.email().isEmpty()) {
            logger.warn("Invalid user update attempt: {}", userDto);
            return ResponseEntity.badRequest().body(null); // 400 Bad Request
        }

        try {
            // JWT에서 가져온 userId로 사용자 업데이트
            UserDto updatedUser = userService.updateUser(userId, userDto.username(), userDto.email(), userDto.password());
            if (updatedUser == null) {
                logger.warn("User not found with ID: {}", userId);
                return ResponseEntity.notFound().build(); // 404 Not Found
            }
            logger.info("User updated: {}", updatedUser.username());
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            logger.error("Error updating user with ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 Internal Server Error
        }
    }





    // 비밀번호 업데이트
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable Long id, @RequestParam String newPassword) {
        userService.updatePassword(id, newPassword); // 비밀번호 업데이트
        logger.info("Password updated for user: {}", id); // 로그 추가
        return ResponseEntity.noContent().build(); // 204 No Content
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
    @GetMapping("/me")
    public ResponseEntity<HomeUser> getMe(Authentication authentication) {
        // 인증된 사용자의 username 또는 userId를 가져옵니다.
        String username = authentication.getName(); // 사용자 이름을 가져옵니다.

        // UserService를 통해 사용자 정보 조회
        Optional<HomeUser> userDto = userService.getUserByUsername(username);

        return userDto
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

}
