package com.spring.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "user_schedule")
public class UserSchedule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user_Id", referencedColumnName = "userId", nullable = false)
  private AllUser userId;

  @Column(nullable = false)
  private LocalDate date; // 일정 날짜

  @Column(nullable = false)
  private String activity; // 활동 내용

  @Column(nullable = false)
  private String timeSlot; // 일정 시간대 (오전 9시, 오후 3시 등)

  @Column(nullable = false)
  private LocalDateTime timestamp;  // 날씨 데이터 생성 시간
}

