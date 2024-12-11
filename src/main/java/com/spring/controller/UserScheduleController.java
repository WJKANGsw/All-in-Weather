package com.spring.controller;

import com.spring.model.UserLocation;
import com.spring.model.UserSchedule;
import com.spring.model.WeatherData;
import com.spring.model.schedule.ScheduleRequest;
import com.spring.model.schedule.ScheduleUpdateRequest;
import com.spring.repository.UserLocationRepository;
import com.spring.service.weather.UserScheduleService;
import com.spring.service.weather.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class UserScheduleController {

  private final UserScheduleService userScheduleService;
  private final WeatherService weatherService;

  // 일정 추가 엔드포인트
  @PostMapping
  public ResponseEntity<UserSchedule> addSchedule(@RequestBody ScheduleRequest request) {
    // 일정 저장
    UserSchedule newSchedule = userScheduleService.addSchedule(
        request.getUserId(),
        request.getDate(),
        request.getActivity(),
        request.getTimeSlot(),
        request.getPlaceName(),
        request.getAddress(),
        request.getTimestamp(),
        request.getLatitude(),
        request.getLongitude()
    );

    // 위도와 경도가 제공되지 않은 경우
    if (request.getLatitude() == null || request.getLongitude() == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(null); // 위도/경도가 없는 경우 400 오류 반환
    }

    // 날씨 데이터 저장
    weatherService.saveWeatherData(
        request.getUserId(),
        request.getLatitude(),
        request.getLongitude(),
        request.getTimestamp()
    );

    return ResponseEntity.ok(newSchedule);
  }

  // 예외 처리 메서드
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
    String errorMessage = "중복된 일정입니다. 같은 시간대에 다른 일정을 등록할 수 없습니다.";
    return ResponseEntity.badRequest().body(errorMessage);
  }

  // 일정 조회 엔드포인트 (사용자 ID로 일정 전체 조회)
  @GetMapping("/{userId}")
  public ResponseEntity<List<UserSchedule>> getUserSchedules(@PathVariable String userId) {
    List<UserSchedule> schedules = userScheduleService.getUserSchedules(userId);
    return ResponseEntity.ok(schedules);
  }

  // 일정 조회 엔드포인트 (특정 날짜로 일정 조회)
  @GetMapping("/{userId}/{date}")
  public ResponseEntity<List<UserSchedule>> getUserSchedulesByDate(@PathVariable String userId, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    List<UserSchedule> schedules = userScheduleService.getUserSchedulesByDate(userId, date);
    return ResponseEntity.ok(schedules);
  }

  // 일정 삭제 엔드포인트 (사용자 ID와 삭제할 일정 ID로 삭제)
  @DeleteMapping("/{userId}/{scheduleId}")
  public ResponseEntity<Void> deleteSchedule(@PathVariable String userId, @PathVariable Long scheduleId) {
    userScheduleService.deleteSchedule(userId, scheduleId);
    return ResponseEntity.noContent().build();
  }

  // 일정 변경
  @PutMapping("/{userId}/{scheduleId}")
  public ResponseEntity<UserSchedule> updateSchedule(
      @PathVariable String userId,
      @PathVariable Long scheduleId,
      @RequestBody ScheduleUpdateRequest updateRequest) {
    UserSchedule updatedSchedule = userScheduleService.updateSchedule(
        userId,
        scheduleId,
        updateRequest.getActivity(),
        updateRequest.getTimeSlot(),
        updateRequest.getPlaceName(),
        updateRequest.getAddress(),
        updateRequest.getLatitude(),
        updateRequest.getLongitude(),
        updateRequest.getTimestamp()
    );
    if (updatedSchedule != null) {
      return ResponseEntity.ok(updatedSchedule);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  // 일정의 날씨 정보 조회
  @GetMapping("/weather")
  public ResponseEntity<WeatherData> getWeatherDetails(
      @RequestParam String userId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestamp
  ) {
    // WeatherData 엔티티에서 사용자 ID와 타임스탬프 기준으로 데이터 조회
    WeatherData weatherData = weatherService.getWeatherDataByUserIdAndTimestamp(userId, timestamp);

    if (weatherData != null) {
      return ResponseEntity.ok(weatherData); // 데이터가 있으면 반환
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 데이터가 없으면 404 반환
    }
  }

}