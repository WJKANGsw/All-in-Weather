package com.spring.service;

import com.spring.model.social_dto.*;
import com.spring.model.social_entity.SocialUserEntity;
import com.spring.repository.UserRepository;
import com.spring.repository.social.SocialUserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final SocialUserRepository userRepository;

    public CustomOAuth2UserService(SocialUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private String generateRandomNickname() {
        String[] adjectives = {"행복한", "멋진", "빛나는", "용감한", "지혜로운"}; // 한글 형용사
        String[] nouns = {"사자", "호랑이", "독수리", "상어", "불사조"}; // 한글 명사
        int randomAdjectiveIndex = (int) (Math.random() * adjectives.length);
        int randomNounIndex = (int) (Math.random() * nouns.length);
        int randomNumber = (int) (Math.random() * 1000); // 랜덤 숫자

        return adjectives[randomAdjectiveIndex] + nouns[randomNounIndex] + randomNumber; // 조합
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
        else {
            return null;
        }
        String username = oAuth2Response.getProvider()+oAuth2Response.getProviderId();
        SocialUserEntity existData = userRepository.findByUsername(username);

        if (existData == null) {  // 신규 사용자 등록
            SocialUserEntity sc_userEntity = new SocialUserEntity();
            sc_userEntity.setUsername(username);
            sc_userEntity.setName(oAuth2Response.getName());
            sc_userEntity.setEmail(oAuth2Response.getEmail());
            sc_userEntity.setRole("ROLE_USER");

            // 랜덤 닉네임 생성 후 저장
            sc_userEntity.setNickname(generateRandomNickname());

            userRepository.save(sc_userEntity);

            SocialUserDTO sc_userDTO = new SocialUserDTO();
            sc_userDTO.setUsername(username);
            sc_userDTO.setName(oAuth2Response.getName());
            sc_userDTO.setEmail(oAuth2Response.getEmail());
            sc_userDTO.setNickname(generateRandomNickname());
            sc_userDTO.setRole("ROLE_USER");

            return new CustomOAuth2User(sc_userDTO);
        }
        else { // 기존 사용자 업데이트
            existData.setName(oAuth2Response.getName());
            existData.setEmail(oAuth2Response.getEmail());
            userRepository.save(existData);

            SocialUserDTO sc_userDTO = new SocialUserDTO();
            sc_userDTO.setUsername(existData.getUsername());
            sc_userDTO.setName(oAuth2Response.getName());
            sc_userDTO.setEmail(oAuth2Response.getEmail()); // 이메일 추가
            sc_userDTO.setNickname(existData.getNickname());
            sc_userDTO.setRole(existData.getRole());

            return new CustomOAuth2User(sc_userDTO);
        }
    }
}
