package com.spring.service;

import com.spring.model.AllUser;
import com.spring.model.AllUserDto;
import com.spring.repository.AllUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final AllUserRepository allUserRepository;

    // 사용자 업데이트
    public AllUserDto updateUser(String userId, String nickname, String email, Integer age, String gender, Double height, Double weight) {
        Optional<AllUser> userOptional = allUserRepository.findByUserId(userId);
        if (userOptional.isPresent()) {
            AllUser user = userOptional.get();
            user.setUserId(userId);
            user.setNickname(nickname);
            user.setEmail(email);
            user.setAge(age);
            user.setGender(gender);
            user.setHeight(height);
            user.setWeight(weight);

            allUserRepository.save(user);
            return new AllUserDto(user.getUserId(), user.getNickname(),user.getEmail(),user.getAge(),
                user.getGender(),user.getHeight(),user.getWeight());
        }
        return null;
    }
}