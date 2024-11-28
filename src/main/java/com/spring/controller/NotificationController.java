package com.spring.controller;

import com.spring.model.UserSchedule;
import com.spring.model.WeatherData;
import com.spring.model.schedule.NotificationResponse;
import com.spring.service.weather.NotificationService;
import com.spring.service.weather.UserScheduleService;
import com.spring.service.weather.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final UserScheduleService userScheduleService;
  private final WeatherService weatherService;
  private final NotificationService notificationService;

  // 일정 시작 6시간 전 알림을 조회하는 엔드포인트
  @GetMapping("/upcoming")
  public ResponseEntity<List<NotificationResponse>> getUpcomingNotifications(@RequestParam String userId) {
    LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    LocalDateTime checkTime = now.plusMinutes(360);  // 현재 시간부터 6시간 후까지의 알림 확인

    List<UserSchedule> schedules = userScheduleService.getSchedulesBetween(userId, now, checkTime);
    List<NotificationResponse> notifications = new ArrayList<>();

    for (UserSchedule schedule : schedules) {
      WeatherData weatherData = weatherService.getWeatherDataForTimestamp(schedule.getUserId().getUserId(), schedule.getTimestamp());
      if (weatherData != null) {
        String notificationMessage = notificationService.generateNotificationMessage(schedule, weatherData);
        notifications.add(new NotificationResponse(
            "알림",
            notificationMessage,
            schedule.getTimestamp()
        ));
      }
    }

    return ResponseEntity.ok(notifications);
  }
}