package com.spring.controller;

import com.spring.model.HomeUser;
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
    public ResponseEntity<HomeUser> createUser(@RequestParam("username") String username,
                                               @RequestParam("password") String password,
                                               @RequestParam("email") String email) {
        HomeUser user = userService.createUser(username, password, email);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    // 사용자 조회
    @GetMapping("/{id}")
    public ResponseEntity<HomeUser> getUser(@PathVariable Long id) {
        Optional<HomeUser> user = userService.getUser(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 사용자 업데이트
    @PutMapping("/{id}")
    public ResponseEntity<HomeUser> updateUser(@PathVariable Long id,
                                           @RequestParam String username,
                                           @RequestParam String password,
                                           @RequestParam String email) {
        HomeUser updatedUser = userService.updateUser(id, username, password, email);
        return ResponseEntity.ok(updatedUser);
    }

    // 사용자 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}