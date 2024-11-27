package com.spring.service.weather;

import com.spring.model.UserSchedule;
import com.spring.model.WeatherData;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationService {

  public String generateNotificationMessage(UserSchedule schedule, WeatherData weatherData) {
    String activity = schedule.getActivity();
    LocalDateTime timestamp = schedule.getTimestamp();
    StringBuilder message = new StringBuilder();

    message.append(String.format("%s에 예정된 \"%s\" 활동이 ", timestamp, activity));

    // 기온 조건
    if (weatherData.getTemperature() < 0) {
      message.append("기온이 매우 낮습니다. 따뜻하게 입으시고 조심하세요. ");
    } else if (weatherData.getTemperature() > 30) {
      message.append("기온이 매우 높습니다. 충분한 물을 마시고 더위를 조심하세요. ");
    }

    // 강수량 조건 및 날씨 조건을 통해 비 또는 눈을 구분
    if (weatherData.getPrecipitation() > 50) {
      if (weatherData.getWeatherCondition().contains("비")) {
        message.append("비가 내릴 확률이 높습니다. 우산을 챙기세요. ");
      } else if (weatherData.getWeatherCondition().contains("눈")) {
        message.append("눈이 올 예정입니다. 미끄럼 사고에 주의하세요. ");
      } else {
        message.append("강수가 예상됩니다. 조심하세요. ");
      }
    }

    return message.toString();
  }
}