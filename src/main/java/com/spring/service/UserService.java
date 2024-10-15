package com.spring.service;

import com.spring.model.*;
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

    // 사용자 등록
    @Transactional
    public UserDto createUser(String username, String password, String email) {
        // 비밀번호를 해시화 (예: BCrypt 사용)
        String hashedPassword = hashPassword(password);
        HomeUser user = new HomeUser(username, hashedPassword, email);
        user.setRole(UserRole.USER); // 기본 역할 설정
        user = userRepository.save(user);
        return new UserDto(user.getId(), user.getUsername(), user.getEmail(), null); // 비밀번호는 제외
    }

    // 사용자 조회
    public Optional<UserDto> getUser(Long id) {
        return userRepository.findById(id)
                .map(user -> new UserDto(user.getId(), user.getUsername(), user.getEmail(), null)); // 비밀번호는 제외
    }

    public Optional<HomeUser> getUserByUsername(String username) {
        return Optional.ofNullable(userRepository.findByUsername(username));
    }

    // 사용자 업데이트
    public UserDto updateUser(Long id, String username, String email, String password) {
        Optional<HomeUser> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            HomeUser user = userOptional.get();
            user.setUsername(username);
            user.setEmail(email);

            // 비밀번호가 제공된 경우 업데이트
            if (password != null) {
                user.setPassword(password); // 해싱된 비밀번호를 저장
            }

            userRepository.save(user); // 변경된 사용자 정보를 저장
            return new UserDto(user.getId(), user.getUsername(), user.getEmail(), null);
        }
        return null; // 사용자 찾지 못한 경우
    }

    // 비밀번호 업데이트
    @Transactional
    public void updatePassword(Long id, String newPassword) {
        HomeUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setPassword(hashPassword(newPassword)); // 비밀번호 해시화
    }

    // 비밀번호 해시화 메서드 (예: BCrypt)
    private String hashPassword(String password) {
        // 비밀번호 해시화 로직 구현 (BCrypt 사용 예시)
        return password; // 여기서는 단순히 리턴 (해시화 로직 필요)
    }

    // 사용자 삭제
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}