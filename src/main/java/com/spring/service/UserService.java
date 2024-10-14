package com.spring.service;

import com.spring.model.HomeUser;
import com.spring.model.UserAlreadyExistsException;
import com.spring.model.UserDto;
import com.spring.model.UserNotFoundException;
import com.spring.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // 비밀번호 인코더 추가

    @Transactional
    public UserDto createUser(String username, String password, String email) {
        HomeUser user = HomeUser.builder()
                .username(username)
                .password(passwordEncoder.encode(password)) // 비밀번호 해싱
                .email(email)
                .build();
        userRepository.save(user);
        return new UserDto(user.getId(), user.getUsername(), user.getEmail()); // DTO 반환
    }

    public Optional<UserDto> getUser(Long id) {
        return userRepository.findById(id)
                .map(user -> new UserDto(user.getId(), user.getUsername(), user.getEmail()));
    }

    @Transactional
    public UserDto updateUser(Long id, String username, String email) {
        HomeUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setUsername(username);
        user.setEmail(email);
        userRepository.save(user);
        return new UserDto(user.getId(), user.getUsername(), user.getEmail());
    }

    @Transactional
    public void updatePassword(Long id, String newPassword) {
        HomeUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword)); // 비밀번호 해싱
        userRepository.save(user); // 사용자 정보를 저장합니다.
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public boolean userExistsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
