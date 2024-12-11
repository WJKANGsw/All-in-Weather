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
  private AllUser userId; // asdf1234

  @Column(nullable = false)
  private LocalDate date; // 일정 날짜 (12월 9일)

  @Column(nullable = false)
  private String activity; // 활동 내용 (등산, 산책, 스포츠, 직접 입력)

  @Column(nullable = false)
  private String timeSlot; // 일정 시간대 (오전 9시, 오후 3시 등)

  @Column(nullable = false)
  private String placeName; // 장소명 (서경대학교 )

  @Column(nullable = false)
  private String address; // 주소명 (서울 성북구 서경로 124)

  @Column(nullable = false)
  private LocalDateTime timestamp;  // 날씨 데이터 생성 시간

  @Column(nullable = true) // 목적지 위도 (null 허용)
  private Double latitude;

  @Column(nullable = true) // 목적지 경도 (null 허용)
  private Double longitude;
}

