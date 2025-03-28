package com.spring.controller;

import com.spring.model.UserLocation;
import com.spring.model.UserSchedule;
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
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class UserScheduleController {

  private final UserScheduleService userScheduleService;
  private final UserLocationRepository userLocationRepository;
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
        request.getTimestamp()
    );

    // 사용자 위치 정보 조회
    UserLocation userLocation = userLocationRepository.findFirstByUserIdOrderByIdDesc(request.getUserId());
    if (userLocation != null) {
      weatherService.saveWeatherData(
          request.getUserId(),
          userLocation.getLatitude(),
          userLocation.getLongitude(),
          request.getTimestamp());
    } else {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

    return ResponseEntity.ok(newSchedule);
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
    UserSchedule updatedSchedule = userScheduleService.updateSchedule(userId, scheduleId, updateRequest.getActivity());
    if (updatedSchedule != null) {
      return ResponseEntity.ok(updatedSchedule);
    } else {
      return ResponseEntity.notFound().build();
    }
  }
}