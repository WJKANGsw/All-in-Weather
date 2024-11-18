package com.spring.service;

import com.spring.common.CertificationNumber;
import com.spring.model.AllUser;
import com.spring.model.AllUserDto;
import com.spring.model.CertificationEntity;
import com.spring.provider.EmailProvider;
import com.spring.repository.AllUserRepository;
import com.spring.repository.CertificationRepository;
import com.spring.repository.UserRepository;
import com.spring.repository.social.SocialUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

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