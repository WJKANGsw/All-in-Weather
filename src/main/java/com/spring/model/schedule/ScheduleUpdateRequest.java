package com.spring.model.schedule;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ScheduleUpdateRequest {
  private String activity;
  private String timeSlot;
  private String placeName;
  private String address;
  private Double latitude;
  private Double longitude;
  private LocalDateTime timestamp;
}
