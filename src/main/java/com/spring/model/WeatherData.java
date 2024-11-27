package com.spring.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "weather_data")
@Data
public class WeatherData {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String userId;  // 사용자를 구별하기 위한 필드

  @Column(nullable = false)
  private Double latitude;

  @Column(nullable = false)
  private Double longitude;

  private Double temperature;

  @Column(name = "high_temperature")
  private Double highTemperature;

  @Column(name = "low_temperature")
  private Double lowTemperature;

  @Column(name = "weather_condition")
  private String weatherCondition;

  private Double precipitation;

  @Column(nullable = false)
  private LocalDateTime timestamp;  // 날씨 데이터 생성 시간
}
