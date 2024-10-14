package com.spring.service;

import com.spring.model.HomeUser;
import com.spring.model.UserAlreadyExistsException;
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
    public HomeUser createUser(String username, String password, String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("User with email already exists");
        }

        String hashedPassword = passwordEncoder.encode(password); // 비밀번호 해싱
        HomeUser user = HomeUser.builder()
                .username(username)
                .password(hashedPassword)
                .email(email)
                .build();
        return userRepository.save(user);
    }

    public Optional<HomeUser> getUser(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public HomeUser updateUser(Long id, String username, String password, String email) {
        HomeUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // 비밀번호 해싱
        user.setEmail(email);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public boolean userExistsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
