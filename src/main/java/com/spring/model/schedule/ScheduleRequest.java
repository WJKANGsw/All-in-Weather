package com.spring.model.schedule;

import com.spring.model.AllUser;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ScheduleRequest {
  private String userId;
  private LocalDate date;
  private String activity;
  private String timeSlot;
  private String placeName;
  private String address;
  private LocalDateTime timestamp;
  private Double latitude; // 위도
  private Double longitude; // 경도
}