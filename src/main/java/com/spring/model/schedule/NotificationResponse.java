package com.spring.model.schedule;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class NotificationResponse {
  private String title;
  private String message;
  private LocalDateTime timestamp;
}
