package com.spring.service.weather;

import com.spring.model.AllUser;
import com.spring.model.WeatherData;
import com.spring.repository.AllUserRepository;
import com.spring.repository.WeatherDataRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WeatherNotificationService {

  private final NotificationService notificationService;
  private final AllUserRepository userRepository;
  private final WeatherDataRepository weatherDataRepository;

  public WeatherNotificationService(NotificationService notificationService,
                                    AllUserRepository userRepository,
                                    WeatherDataRepository weatherDataRepository) {
    this.notificationService = notificationService;
    this.userRepository = userRepository;
    this.weatherDataRepository = weatherDataRepository;
  }

  public void notifyUsersAboutWeather() {
    // 특정 시간대에 대한 데이터 조회 (예: 현재 시간 기준으로 다음 1시간 내 데이터)
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime sixHourLater = now.plusHours(6);

    List<WeatherData> weatherDataList = weatherDataRepository.findByTimestampBetween(now, sixHourLater);

    for (WeatherData weatherData : weatherDataList) {
      String userId = weatherData.getUserId();
      String weatherCondition = weatherData.getWeatherCondition();
      double temperature = weatherData.getTemperature();

      // 사용자 정보 및 FCM 토큰 조회
      Optional<AllUser> userOptional = userRepository.findByUserId(userId);
      if (userOptional.isPresent()) {
        AllUser user = userOptional.get();
        String fcmToken = user.getFcmToken();

        if (fcmToken != null) {
          // 알림 메시지 생성
          String title = "날씨 알림";
          String body = String.format("현재 기온은 %.1f도이며 %s입니다. 활동에 참고하세요.", temperature, weatherCondition);

          // FCM 푸시 알림 전송
          notificationService.sendPushNotification(fcmToken, title, body);
        }
      }
    }
  }
}
