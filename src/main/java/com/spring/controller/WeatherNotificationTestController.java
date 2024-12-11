package com.spring.controller;

import com.spring.service.weather.WeatherNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class WeatherNotificationTestController {

  private final WeatherNotificationService weatherNotificationService;

  public WeatherNotificationTestController(WeatherNotificationService weatherNotificationService) {
    this.weatherNotificationService = weatherNotificationService;
  }

  @PostMapping("/notify")
  public ResponseEntity<String> testNotifyUsers() {
    weatherNotificationService.notifyUsersAboutWeather();
    return ResponseEntity.ok("Weather notifications sent successfully.");
  }
}

