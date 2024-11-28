package com.spring.service.weather;

import com.spring.model.UserSchedule;
import com.spring.model.WeatherData;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class NotificationService {

  public String generateNotificationMessage(UserSchedule schedule, WeatherData weatherData) {
    String activity = schedule.getActivity();
    LocalDateTime timestamp = schedule.getTimestamp();
    StringBuilder message = new StringBuilder();

    // 날짜와 시간을 더 읽기 쉬운 포맷으로 변환
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 a h시");
    String formattedTime = timestamp.format(formatter).replace("AM", "오전").replace("PM", "오후");

    // 메시지 시작 부분
    message.append(String.format("%s에 예정된 \"%s\" 활동에 대한 알림입니다. ", formattedTime, activity));

    boolean specialWeatherAlert = false;

    // 기온 조건
    if (weatherData.getTemperature() < 0) {
      message.append("기온이 매우 낮습니다. 따뜻하게 입으시고 조심하세요. ");
      specialWeatherAlert = true;
    } else if (weatherData.getTemperature() > 30) {
      message.append("기온이 매우 높습니다. 충분한 물을 마시고 더위를 조심하세요. ");
      specialWeatherAlert = true;
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
      specialWeatherAlert = true;
    }

    // 특별한 기상 상황이 아니라면 현재 기온을 알려주는 메시지 추가
    if (!specialWeatherAlert) {
      message.append(String.format("현재 예상 기온은 %.1f도입니다. 활동에 참고하세요.", weatherData.getTemperature()));
    }

    return message.toString();
  }
}
