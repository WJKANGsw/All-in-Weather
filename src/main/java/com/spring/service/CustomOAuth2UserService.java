package com.spring.service;

import com.spring.model.AllUser;
import com.spring.model.AllUserDto;
import com.spring.model.LoginType;
import com.spring.model.UserRole;
import com.spring.model.social_dto.*;
import com.spring.repository.AllUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AllUserRepository userRepository;

    private String generateRandomNickname() {
        String[] adjectives = {"행복한", "멋진", "빛나는", "용감한", "지혜로운"}; // 한글 형용사
        String[] nouns = {"사자", "호랑이", "독수리", "상어", "불사조"}; // 한글 명사
        int randomAdjectiveIndex = (int) (Math.random() * adjectives.length);
        int randomNounIndex = (int) (Math.random() * nouns.length);
        int randomNumber = (int) (Math.random() * 1000); // 랜덤 숫자

        return adjectives[randomAdjectiveIndex] + nouns[randomNounIndex] + randomNumber; // 조합
    }

    private boolean isProfileComplete(AllUser user) {
        return user.getAge() != null &&
            user.getGender() != null &&
            user.getHeight() != null &&
            user.getWeight() != null;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println("OAuth2 User Attributes: " + oAuth2User.getAttributes()); // 전체 속성 로그 추가

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;
        if (registrationId.equals("naver")) {
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        }
        else if (registrationId.equals("google")) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        }
        else if (registrationId.equals("kakao")) {
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        }
        else {
            return null;
        }
        String username = oAuth2Response.getProvider()+oAuth2Response.getProviderId();
        Optional<AllUser> existData = userRepository.findByUserId(username);

        if (existData.isEmpty()) {  // 신규 사용자 등록
            AllUser newUser = new AllUser();

            newUser.setUserId(username);
            newUser.setProvider(LoginType.valueOf(oAuth2Response.getProvider().toUpperCase()));// 소셜 로그인 방법 설정
            newUser.setName(oAuth2Response.getName());
            newUser.setEmail(oAuth2Response.getEmail());
            newUser.setNickname(generateRandomNickname());
            newUser.setRole(UserRole.USER);

            newUser.setAge(null);
            newUser.setGender(null);
            newUser.setHeight(null);
            newUser.setWeight(null);
            newUser.setPassword(null); // 소셜 로그인에서는 비밀번호가 필요 없으므로 null
            newUser.setRegistrationDate(LocalDate.now());

            userRepository.save(newUser);

            AllUserDto userDto = new AllUserDto(
                newUser.getUserId(),
                null, // 비밀번호는 null
                newUser.getEmail(),
                newUser.getNickname(),
                false
            );

            return new CustomOAuth2User(userDto);
        }
        else { // 기존 사용자 업데이트
            AllUser existingUser = existData.get(); // Optional에서 실제 객체를 가져옵니다.
            existingUser.setEmail(oAuth2Response.getEmail());
            existingUser.setName(oAuth2Response.getName());
            existingUser.setNickname(generateRandomNickname()); // 랜덤 닉네임 생성 (필요 시)
            userRepository.save(existingUser);

            // 프로필이 완료되었는지 확인
            boolean profileComplete = isProfileComplete(existingUser);

            // DTO로 변환하여 OAuth2User 반환
            AllUserDto userDto = new AllUserDto(
                existingUser.getUserId(),
                null, // 비밀번호는 null
                existingUser.getEmail(),
                existingUser.getNickname(),
                profileComplete
            );

            return new CustomOAuth2User(userDto);
        }
    }
}
