package com.spring.model.social_dto;

import java.util.Map;

public class NaverResponse implements OAuth2Response {

    private final Map<String, Object> attribute;

    public NaverResponse(Map<String, Object> attribute) {
        this.attribute = (Map<String, Object>) attribute.get("response");
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getProviderId() {
        return attribute.get("id").toString();
    }

    @Override
    public String getName() {
        String name = attribute.get("name").toString();
        System.out.println("Naver Name: " + name); // 이름 로그 추가
        return name;
    }

    @Override
    public String getEmail() {
        String email = attribute.get("email").toString();
        System.out.println("Naver Email: " + email); // 이메일 로그 추가
        return email;
    }


}