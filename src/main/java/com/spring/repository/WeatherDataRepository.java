package com.spring.repository;

import com.spring.model.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {

  // 필요한 커스텀 메서드 작성 가능
  WeatherData findTopByLatitudeAndLongitudeOrderByTimestampDesc(Double latitude, Double longitude);

  // userId와 timestamp로 WeatherData 조회
  WeatherData findByUserIdAndTimestamp(String userId, LocalDateTime timestamp);
}
