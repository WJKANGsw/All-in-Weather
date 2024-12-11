package com.spring.repository;

import com.spring.model.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {
  // 필요한 커스텀 메서드 작성 가능
  WeatherData findTopByLatitudeAndLongitudeOrderByTimestampDesc(Double latitude, Double longitude);
  // userId와 timestamp로 WeatherData 조회
  WeatherData findByUserIdAndTimestamp(String userId, LocalDateTime timestamp);

  @Query("SELECT w FROM WeatherData w WHERE w.userId = :userId AND w.timestamp = :timestamp ORDER BY w.id DESC")
  WeatherData findTopByUserIdAndTimestampOrderByIdDesc(@Param("userId") String userId, @Param("timestamp") LocalDateTime timestamp);

  List<WeatherData> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

  WeatherData findTopByUserIdAndTimestampLessThanEqualOrderByTimestampDesc(String userId, LocalDateTime timestamp);

  void deleteByUserIdAndLatitudeAndLongitudeAndTimestamp(String userId, Double latitude, Double longitude, LocalDateTime timestamp);

  void deleteByUserIdAndTimestamp(String userId, LocalDateTime timestamp);
}
