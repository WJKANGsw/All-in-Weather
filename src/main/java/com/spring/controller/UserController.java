package com.spring.controller;

import com.spring.model.HomeUser;
import com.spring.model.UserDto;
import com.spring.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // 사용자 등록
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto, @RequestParam String password) {
        UserDto createdUser = userService.createUser(userDto.getUsername(), password, userDto.getEmail());
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    // 사용자 조회
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        Optional<UserDto> userDto = userService.getUser(id);
        return userDto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 사용자 업데이트 (비밀번호 없이)
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        UserDto updatedUser = userService.updateUser(id, userDto.getUsername(), userDto.getEmail());
        return ResponseEntity.ok(updatedUser);
    }

    // 비밀번호 업데이트
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable Long id, @RequestParam String newPassword) {
        userService.updatePassword(id, newPassword);
        return ResponseEntity.noContent().build();
    }

    // 사용자 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build(); // 200 OK로 응답
    }
}