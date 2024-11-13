package com.spring.model.social_dto;

import com.spring.model.AllUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {
    private final AllUserDto userDTO;


    @Override
    public Map<String, Object> getAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("userId", userDTO.getUserId());
        attributes.put("name", userDTO.getName());
        attributes.put("email", userDTO.getEmail());
        attributes.put("nickname", userDTO.getNickname());
        attributes.put("profileComplete", userDTO.isProfileComplete()); // 추가 정보 여부 추가
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return userDTO.getRole();
            }
        });
        return collection;
    }

    @Override
    public String getName() {
        String name = userDTO.getName();
        return name != null && !name.isEmpty() ? name : "defaultName";
    }

    public String getUsername() {
        return userDTO.getUserId();
    }

    public String getEmail() {
        return userDTO.getEmail();
    }

    public String getRole() {
        return userDTO.getRole();
    }

    public String getNickname() {
        return userDTO.getNickname();
    }

    // 추가 정보 여부 반환 메서드 추가
    public boolean isProfileComplete() {
        return userDTO.isProfileComplete();
    }

    // 새로운 setter 메서드 추가
    public void setNickname(String nickname) {
        this.userDTO.setNickname(nickname);
    }

    public void setName(String name) {
        this.userDTO.setName(name);
    }

    public void setEmail(String email) {
        this.userDTO.setEmail(email);
    }
}
