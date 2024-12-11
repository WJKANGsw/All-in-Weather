package com.spring.controller;

import com.spring.model.AllUser;
import com.spring.model.UserSchedule;
import com.spring.model.WeatherData;
import com.spring.model.schedule.NotificationResponse;
import com.spring.repository.AllUserRepository;
import com.spring.service.weather.NotificationService;
import com.spring.service.weather.UserScheduleService;
import com.spring.service.weather.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final UserScheduleService userScheduleService;
  private final WeatherService weatherService;
  private final NotificationService notificationService;
  private final AllUserRepository userRepository;

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

        // FCM 토큰 가져오기 (이 부분은 사용자별로 다릅니다. 사용자 DB에서 FCM 토큰을 가져와야 함)
        String userFcmToken = schedule.getUserId().getFcmToken(); // 사용자 토큰을 데이터베이스에서 가져옴

        // FCM 푸시 알림 전송
        if (userFcmToken != null) {
          notificationService.sendPushNotification(userFcmToken, "알림", notificationMessage);
        }
      }
    }

    return ResponseEntity.ok(notifications);
  }

  @PostMapping("/send")
  public ResponseEntity<String> sendPushNotificationTest(@RequestParam String userId) {
    // 사용자 정보 가져오기
    Optional<AllUser> userOptional = userRepository.findByUserId(userId);
    if (userOptional.isPresent()) {
      AllUser user = userOptional.get();
      String fcmToken = user.getFcmToken();

      if (fcmToken != null) {
        // 테스트용 알림 제목과 본문
        String title = "테스트 알림";
        String body = "백그라운드 푸시 알림 테스트입니다. 이 알림은 FCM을 통해 전송되었습니다.";

        // FCM 푸시 알림 전송
        notificationService.sendPushNotification(fcmToken, title, body);

        return ResponseEntity.ok("푸시 알림이 전송되었습니다.");
      } else {
        return ResponseEntity.badRequest().body("FCM 토큰이 없습니다.");
      }
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
    }
  }



}