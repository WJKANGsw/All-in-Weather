package com.spring.model.social_dto;

import java.util.Map;

public class GoogleResponse implements OAuth2Response {

    private final Map<String, Object> attribute;

    public GoogleResponse(Map<String, Object> attribute) {
        this.attribute = attribute;
    }

    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String getProviderId() {
        return attribute.get("sub").toString();
    }

    @Override
    public String getName() {
        String name = attribute.get("name").toString();
        System.out.println("Google Name: " + name); // 이름 로그 추가
        return name;
    }

    @Override
    public String getEmail() {
        String email = attribute.get("email").toString();
        System.out.println("Google Email: " + email); // 이메일 로그 추가
        return email;
    }


}
