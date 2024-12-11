package com.spring.model.schedule;

import lombok.Data;

@Data
public class UpdateFcmTokenRequest {
  private String userId;
  private String fcmToken;

  // getters and setters
}