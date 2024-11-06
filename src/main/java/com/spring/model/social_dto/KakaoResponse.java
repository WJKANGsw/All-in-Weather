package com.spring.model.social_dto;

import java.util.Map;

public class KakaoResponse implements OAuth2Response {

    private final Map<String, Object> attribute;
    private final Map<String, Object> properties;
    private final Map<String, Object> kakaoAccount;

    public KakaoResponse(Map<String, Object> attribute) {
        this.attribute = attribute;
        this.properties = (Map<String, Object>) attribute.get("properties");
        this.kakaoAccount = (Map<String, Object>) attribute.get("kakao_account");
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        Object id = attribute.get("id");
        return id != null ? id.toString() : null;
    }

    @Override
    public String getName() {
        if (properties != null) {
            Object nickname = properties.get("nickname");
            return nickname != null ? nickname.toString() : "Unknown"; // 기본값 또는 예외 처리
        }
        return "Unknown";
    }

    @Override
    public String getEmail() {
        if (kakaoAccount != null) {
            Object email = kakaoAccount.get("email");
            return email != null ? email.toString() : null;
        }
        return null;
    }
}
