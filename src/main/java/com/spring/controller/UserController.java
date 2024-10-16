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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class); // Logger 추가

    // 사용자 등록
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        UserDto createdUser = userService.createUser(userDto.username(), userDto.password(), userDto.email());
        logger.info("User registered: {}", createdUser.username()); // 로그 추가
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    // 사용자 조회
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        Optional<UserDto> userDto = userService.getUser(id);
        if (userDto.isPresent()) {
            logger.info("User retrieved: {}", userDto.get().username()); // 로그 추가
            return ResponseEntity.ok(userDto.get());
        } else {
            logger.warn("User not found: {}", id); // 로그 추가
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        String encryptedPassword = null;

        // 비밀번호가 제공된 경우
        if (userDto.password() != null && !userDto.password().isEmpty()) {
            encryptedPassword = passwordEncoder.encode(userDto.password());
        }

        UserDto updatedUser = userService.updateUser(id, userDto.username(), userDto.email(), encryptedPassword);
        logger.info("User updated: {}", updatedUser.username()); // 로그 추가

        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable Long id, @RequestParam String newPassword) {
        String encryptedPassword = passwordEncoder.encode(newPassword);
        userService.updatePassword(id, encryptedPassword); // 비밀번호 업데이트
        logger.info("Password updated for user: {}", id); // 로그 추가
        return ResponseEntity.noContent().build();
    }

    // 사용자 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        logger.info("User deleted: {}", id); // 로그 추가
        return ResponseEntity.ok().build(); // 200 OK로 응답
    }

    @PostMapping("/register") // '/api/users/register' 경로로 요청을 처리
    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto) {
        String encryptedPassword = passwordEncoder.encode(userDto.password());
        UserDto createdUser = userService.createUser(userDto.username(), encryptedPassword, userDto.email());
        logger.info("User registered via register endpoint: {}", createdUser.username()); // 로그 추가
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody UserDto userDto) {
        HomeUser user = userService.getUserByUsername(userDto.username())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(userDto.password(), user.getPassword())) {
            logger.warn("Invalid password attempt for user: {}", userDto.username()); // 로그 추가
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid password"));
        }

        String token = jwtTokenProvider.createToken(user.getUsername(), List.of(user.getRole().getValue()));
        logger.info("User logged in: {}", user.getUsername()); // 로그 추가
        return ResponseEntity.ok(Map.of("token", token)); // JSON 객체로 반환
    }
}