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
    private final PasswordEncoder passwordEncoder; // BCryptPasswordEncoder 주입

    // 사용자 등록
    @Transactional
    public UserDto createUser(String username, String userId, String password, String email) {
        // 비밀번호를 해시화 (예: BCrypt 사용)
        String hashedPassword = hashPassword(password);
        HomeUser user = new HomeUser(null, username, userId, hashedPassword, email, UserRole.USER); // ID는 null로 설정
        user = userRepository.save(user);
        return new UserDto(user.getId(), user.getUsername(), user.getUserId(), user.getEmail(), null); // 비밀번호는 제외
    }


    // 사용자 조회
    public Optional<UserDto> getUser(Long id) {
        return userRepository.findById(id)
                .map(user -> new UserDto(user.getId(), user.getUsername(), user.getUserId(),user.getEmail(), null)); // 비밀번호는 제외
    }

    public Optional<HomeUser> getUserByUsername(String username) {
        return Optional.ofNullable(userRepository.findByUsername(username));
    }

//    public Optional<UserDto> getUserUserId(String username) {
//        return userRepository.findByUsername(username)
//                .map(user -> new UserDto(user.getId(), user.getUsername(), user.getEmail(), null)); // 비밀번호는 제외
//    }

    public Optional<HomeUser> getUserById(String userId) {
        return userRepository.findByUserId(userId)
                .map(user -> new HomeUser(user.getId(), user.getUsername(), user.getUserId(), user.getPassword(), user.getEmail(), user.getRole()));
    }


    // 사용자 업데이트
    public UserDto updateUser(Long id, String userId, String username, String email, String password) {
        Optional<HomeUser> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            HomeUser user = userOptional.get();
            user.setUserId(userId);
            user.setUsername(username);
            user.setEmail(email);

            // 비밀번호가 제공된 경우 해싱 후 업데이트
            if (password != null) {
                user.setPassword(hashPassword(password)); // 해싱된 비밀번호 저장
            }

            userRepository.save(user); // 변경된 사용자 정보를 저장
            return new UserDto(user.getId(),user.getUserId(), user.getUsername(), user.getEmail(), null);
        }
        return null; // 사용자 찾지 못한 경우
    }

    public UserDto updateUserId(String userId){
        Optional<HomeUser> userOptional = userRepository.findByUserId(userId);
        if (userOptional.isPresent()) {
            HomeUser user = userOptional.get();
            user.setUserId(userId);

            userRepository.save(user);
            new UserDto(user.getId(), user.getUsername(), user.getUserId(), user.getEmail(), null);
        }
        return null;
    }

    // 비밀번호 업데이트
    @Transactional
    public void updatePassword(String userId, String newPassword) {
        HomeUser user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setPassword(hashPassword(newPassword)); // 비밀번호 해시화
    }

    // 비밀번호 해시화 메서드
    private String hashPassword(String password) {
        return passwordEncoder.encode(password); // BCryptPasswordEncoder로 비밀번호 인코딩
    }

    public boolean checkPassword(HomeUser user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    // 사용자 삭제
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}