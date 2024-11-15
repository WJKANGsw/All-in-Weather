package com.spring.service;

import com.spring.common.CertificationNumber;
import com.spring.model.*;
import com.spring.model.social_dto.SocialUserDTO;
import com.spring.model.social_entity.SocialUserEntity;
import com.spring.provider.EmailProvider;
import com.spring.repository.AllUserRepository;
import com.spring.repository.CertificationRepository;
import com.spring.repository.UserRepository;
import com.spring.repository.social.SocialUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class UserService {

    private final SocialUserRepository socialUserRepository;
    private final UserRepository userRepository;
    private final AllUserRepository allUserRepository;
    private final PasswordEncoder passwordEncoder; // BCryptPasswordEncoder 주입
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final CertificationRepository certificationRepository; // 인증 레포지토리 주입
    private final EmailProvider emailProvider; // 이메일 전송 컴포넌트 주입


    // homeuser 사용자 등록
    @Transactional
    public UserDto createUser(String username, String userId, String password, String email, Integer age) {
        String hashedPassword = hashPassword(password);
        HomeUser user = new HomeUser(null, username, userId, hashedPassword, email, UserRole.USER, age); // ID는 null로 설정
        user = userRepository.save(user);
        return new UserDto(user.getId(), user.getUsername(), user.getUserId(), user.getEmail(), null, user.getAge()); // 비밀번호는 제외
    }

    // 사용자 조회
    public Optional<HomeUser> getUserById(String userId) {
        return userRepository.findByUserId(userId)
                .map(user -> new HomeUser(user.getId(), user.getUsername(), user.getUserId(), user.getPassword(), user.getEmail(), user.getRole(), user.getAge())); // age 필드 추가
    }

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

    // 일반로그인 아이디 변경
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

    // 일반로그인 비밀번호 확인
    public boolean checkPassword(HomeUser user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    // 일반로그인 사용자 삭제
    @Transactional
    public void deleteUser(String userId) {
        HomeUser user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        userRepository.delete(user);
    }

    //일반로그인 비밀번호 검증
    public boolean verifypassword(String userId, String rawPassword) {
        HomeUser user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    // 소셜로그인 사용자 닉네임, 이메일, 이름 변경
    @Transactional
    public SocialUserDTO updateSocialUser(String username, SocialUserDTO socialUserDTO) {
        Optional<SocialUserEntity> socialUserOptional = Optional.ofNullable(socialUserRepository.findByUserId(username));
        if (socialUserOptional.isPresent()) {
            SocialUserEntity socialUser = socialUserOptional.get();

            // 업데이트 전 데이터 로그
            System.out.println("Before update - Nickname: " + socialUser.getNickname() + ", Name: " + socialUser.getName() + ", Email: " + socialUser.getEmail());

            // 소셜 사용자 정보 업데이트
            socialUser.setNickname(socialUserDTO.getNickname());
            socialUser.setName(socialUserDTO.getName());
            socialUser.setEmail(socialUserDTO.getEmail());

            // 저장
            socialUserRepository.save(socialUser);

            // 업데이트 후 데이터 로그
            System.out.println("After update - Nickname: " + socialUser.getNickname() + ", Name: " + socialUser.getName() + ", Email: " + socialUser.getEmail());

            // 기존 SocialUserDTO 객체에 값을 설정
            socialUserDTO.setUserId(socialUser.getUserId());
            socialUserDTO.setName(socialUser.getName());
            socialUserDTO.setEmail(socialUser.getEmail());
            socialUserDTO.setRole(socialUser.getRole());
            socialUserDTO.setNickname(socialUser.getNickname());
            return socialUserDTO; // 업데이트된 SocialUserDTO 반환
        }
        // 사용자 없을 경우 null 반환 (혹은 예외 처리 가능)
        return null;
    }

    // 소셜로그인 사용자 삭제
    @Transactional
    public void deleteSocialUser(String username) {
        SocialUserEntity socialUser = socialUserRepository.findByUserId(username);
        if (socialUser != null) {
            socialUserRepository.delete(socialUser);
            logger.info("Deleted social user with username: {}", username);
        } else {
            throw new UsernameNotFoundException("Social user not found");
        }
    }

    // Id중복체크 있으면 true를 return
    public boolean userIdExists(String userId) {
        return userRepository.existsByUserId(userId);
    }

    // email 중복체크 있으면 true를 return
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    // email 인증 버튼을 누르면 인증코드를 전송
    @Transactional
    public boolean sendEmailCertification(String userId, String email) {
        if (userRepository.existsByUserId(userId)) {
            logger.warn("User ID already exists: {}", userId);
            return false; // 이미 존재하는 ID인 경우 실패 처리
        }

        // 인증 코드 생성 및 메일 전송
        String certificationNumber = CertificationNumber.getCertificationNumber();
        boolean isSent = emailProvider.sendCertificationMail(email, certificationNumber);

        if (isSent) {
            // 인증 정보 저장
            CertificationEntity entity = new CertificationEntity(userId, email, certificationNumber);
            certificationRepository.save(entity);
            return true;
        }

        return false; // 메일 전송 실패 시
    }

    // 이메일 인증코드 검증
    @Transactional
    public boolean verifyCertificationCode(String userId, String email, String certificationNumber) {
        CertificationEntity certificationEntity = certificationRepository.findByUserId(userId);

        if (certificationEntity != null && certificationEntity.getEmail().equals(email) &&
            certificationEntity.getCertificationNumber().equals(certificationNumber)) {
            certificationRepository.delete(certificationEntity); // 인증 성공 시 데이터 삭제
            return true;
        }

        return false; // 인증 실패 시
    }
}