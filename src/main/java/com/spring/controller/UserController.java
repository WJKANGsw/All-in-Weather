package com.spring.controller;

import com.spring.model.HomeUser;
import com.spring.model.UserDto;
import com.spring.security.JwtTokenProvider;
import com.spring.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider; // 추가

    // 사용자 등록
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        UserDto createdUser = userService.createUser(userDto.username(), userDto.password(), userDto.email());
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    // 사용자 조회
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        Optional<UserDto> userDto = userService.getUser(id);
        return userDto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        String encryptedPassword = null;

        // 비밀번호가 제공된 경우
        if (userDto.password() != null && !userDto.password().isEmpty()) {
            encryptedPassword = passwordEncoder.encode(userDto.password());
        }

        // 사용자 정보를 업데이트 (비밀번호가 null인 경우 비밀번호 업데이트 없이)
        UserDto updatedUser = userService.updateUser(id, userDto.username(), userDto.email(), encryptedPassword);

        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable Long id, @RequestParam String newPassword) {
        String encryptedPassword = passwordEncoder.encode(newPassword);
        userService.updatePassword(id, encryptedPassword); // 비밀번호 업데이트
        return ResponseEntity.noContent().build();
    }

    // 사용자 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build(); // 200 OK로 응답
    }

    @PostMapping("/register") // '/api/users/register' 경로로 요청을 처리
    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto) {
        // 비밀번호를 BCrypt로 인코딩
        String encryptedPassword = passwordEncoder.encode(userDto.password());
        UserDto createdUser = userService.createUser(userDto.username(), encryptedPassword, userDto.email());
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserDto userDto) {
        HomeUser user = userService.getUserByUsername(userDto.username())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(userDto.password(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password");
        }

        // JWT 토큰 생성
        String token = jwtTokenProvider.createToken(user.getUsername(), List.of(user.getRole().getValue()));
        return ResponseEntity.ok(token);
    }
}