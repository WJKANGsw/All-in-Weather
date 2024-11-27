package com.spring.model.schedule;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WeatherDataRequest {
  private String userId;
  private Double latitude;
  private Double longitude;
  private Double temperature;
  private Double highTemperature;
  private Double lowTemperature;
  private String weatherCondition;
  private Double precipitation;
  private LocalDateTime timestamp;
  private String timeSlot;
}
