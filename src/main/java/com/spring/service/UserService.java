package com.spring.service;

import com.spring.model.*;
import com.spring.model.social_dto.SocialUserDTO;
import com.spring.model.social_entity.SocialUserEntity;
import com.spring.repository.UserRepository;
import com.spring.repository.social.SocialUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final SocialUserRepository socialUserRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // BCryptPasswordEncoder 주입

    // 사용자 등록
    @Transactional
    public UserDto createUser(String username, String userId, String password, String email, Integer age) {
        String hashedPassword = hashPassword(password);
        HomeUser user = new HomeUser(null, username, userId, hashedPassword, email, UserRole.USER, age); // ID는 null로 설정
        user = userRepository.save(user);
        return new UserDto(user.getId(), user.getUsername(), user.getUserId(), user.getEmail(), null, user.getAge()); // 비밀번호는 제외
    }

    public Optional<HomeUser> getUserByUsername(String username) {
        return Optional.ofNullable(userRepository.findByUsername(username));
    }

    // 사용자 조회
    public Optional<UserDto> getUser(Long id) {
        return userRepository.findById(id)
                .map(user -> new UserDto(user.getId(), user.getUsername(), user.getUserId(), user.getEmail(), null, user.getAge())); // 비밀번호는 제외
    }

    public Optional<HomeUser> getUserById(String userId) {
        return userRepository.findByUserId(userId)
                .map(user -> new HomeUser(user.getId(), user.getUsername(), user.getUserId(), user.getPassword(), user.getEmail(), user.getRole(), user.getAge())); // age 필드 추가
    }

    // 사용자 업데이트
    public UserDto updateUser(Long id, String userId, String username, String email, String password, Integer age) {
        Optional<HomeUser> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            HomeUser user = userOptional.get();
            user.setUserId(userId);
            user.setUsername(username);
            user.setEmail(email);
            user.setAge(age); // 나이 업데이트

            if (password != null) {
                user.setPassword(hashPassword(password));
            }

            userRepository.save(user);
            return new UserDto(user.getId(), user.getUsername(), user.getUserId(), user.getEmail(), null, user.getAge());
        }
        return null;
    }

    public UserDto updateUserId(String userId){
        Optional<HomeUser> userOptional = userRepository.findByUserId(userId);
        if (userOptional.isPresent()) {
            HomeUser user = userOptional.get();
            user.setUserId(userId);

            userRepository.save(user);
            return new UserDto(user.getId(), user.getUsername(), user.getUserId(), user.getEmail(), null, user.getAge()); // 수정된 UserDto 반환
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

    @Transactional
    public SocialUserDTO updateSocialUser(String username, SocialUserDTO socialUserDTO) {
        Optional<SocialUserEntity> socialUserOptional = Optional.ofNullable(socialUserRepository.findByUsername(username));

        if (socialUserOptional.isPresent()) {
            SocialUserEntity socialUser = socialUserOptional.get();

            // 소셜 사용자 정보 업데이트
            socialUser.setNickname(socialUserDTO.getNickname());
            socialUser.setName(socialUserDTO.getName());
            socialUser.setEmail(socialUserDTO.getEmail());

            // 저장
            socialUserRepository.save(socialUser);

            // 기존 SocialUserDTO 객체에 값을 설정
            socialUserDTO.setUsername(socialUser.getUsername());
            socialUserDTO.setName(socialUser.getName());
            socialUserDTO.setEmail(socialUser.getEmail());
            socialUserDTO.setRole(socialUser.getRole());
            socialUserDTO.setNickname(socialUser.getNickname());

            return socialUserDTO; // 업데이트된 SocialUserDTO 반환
        }
        // 사용자 없을 경우 null 반환 (혹은 예외 처리 가능)
        return null;
    }


}