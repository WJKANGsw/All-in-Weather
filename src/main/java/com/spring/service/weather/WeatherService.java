package com.spring.service.weather;

import com.fasterxml.jackson.databind.JsonNode;
import com.spring.model.WeatherData;
import com.spring.repository.WeatherDataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.*;

@Service
public class WeatherService {

  @Value("${weather.api.key}")
  private String weatherApiKey;

  private final RestTemplate restTemplate;
  private final WeatherDataRepository weatherDataRepository;

  public WeatherService(RestTemplate restTemplate, WeatherDataRepository weatherDataRepository) {
    this.restTemplate = restTemplate;
    this.weatherDataRepository = weatherDataRepository;
  }

  // 위치 정보와 정확한 timestamp를 기반으로 날씨 데이터를 저장하는 메서드
  public void saveWeatherData(String userId, double latitude, double longitude, LocalDateTime timestamp) {
    String url = String.format(
        "https://api.openweathermap.org/data/3.0/onecall?lat=%f&lon=%f&appid=%s&units=metric&lang=kr",
        latitude, longitude, weatherApiKey);
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);

    if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
      System.err.println("Failed to fetch weather data. Response Code: " + response.getStatusCode());
      throw new RuntimeException("Failed to fetch weather data from the API");
    }

    JsonNode data = response.getBody();

    if (Duration.between(LocalDateTime.now(), timestamp).toHours() > 48) {
      // 48시간 이후: daily 데이터 사용
      JsonNode targetDayData = findWeatherDataForDate(data, timestamp.toLocalDate());
      if (targetDayData != null) {
        saveDailyWeatherData(userId, latitude, longitude, targetDayData, timestamp);
      }
    } else {
      // 48시간 이내: hourly 데이터 사용
      JsonNode targetHourData = findWeatherDataForTimestamp(data, timestamp);
      if (targetHourData != null) {
        saveHourlyWeatherData(userId, latitude, longitude, targetHourData, timestamp);
      }
    }
  }

  // 특정 timestamp에 해당하는 저장된 날씨 데이터를 반환하는 메서드
  public WeatherData getWeatherDataForTimestamp(String userId, LocalDateTime timestamp) {
    return weatherDataRepository.findByUserIdAndTimestamp(userId, timestamp);
  }

  // daily 데이터로 날씨 정보를 저장하는 메서드
  private void saveDailyWeatherData(String userId, double latitude, double longitude, JsonNode dailyData, LocalDateTime timestamp) {
    WeatherData weatherData = new WeatherData();
    weatherData.setUserId(userId);
    weatherData.setLatitude(latitude);
    weatherData.setLongitude(longitude);
    weatherData.setTemperature(dailyData.path("temp").path("day").asDouble());
    weatherData.setHighTemperature(dailyData.path("temp").path("max").asDouble());
    weatherData.setLowTemperature(dailyData.path("temp").path("min").asDouble());
    weatherData.setWeatherCondition(dailyData.path("weather").get(0).path("description").asText());
    weatherData.setPrecipitation(dailyData.path("pop").asDouble() * 100);
    weatherData.setTimestamp(timestamp);

    try {
      weatherDataRepository.save(weatherData);
      System.out.println("Weather data saved successfully (Daily Data)");
    } catch (Exception e) {
      System.err.println("Error saving daily weather data: " + e.getMessage());
    }
  }

  // hourly 데이터로 날씨 정보를 저장하는 메서드
  private void saveHourlyWeatherData(String userId, double latitude, double longitude, JsonNode hourlyData, LocalDateTime timestamp) {
    WeatherData weatherData = new WeatherData();
    weatherData.setUserId(userId);
    weatherData.setLatitude(latitude);
    weatherData.setLongitude(longitude);
    weatherData.setTemperature(hourlyData.path("temp").asDouble());
    weatherData.setHighTemperature(hourlyData.path("temp").asDouble()); // hourly 데이터에서는 max, min이 없음
    weatherData.setLowTemperature(hourlyData.path("temp").asDouble());
    weatherData.setWeatherCondition(hourlyData.path("weather").get(0).path("description").asText());
    weatherData.setPrecipitation(hourlyData.path("pop").asDouble() * 100);
    weatherData.setTimestamp(timestamp);

    try {
      weatherDataRepository.save(weatherData);
      System.out.println("Weather data saved successfully (Hourly Data)");
    } catch (Exception e) {
      System.err.println("Error saving hourly weather data: " + e.getMessage());
    }
  }

  // 특정 날짜에 해당하는 날씨 데이터를 찾는 메서드 (daily 데이터 사용)
  private JsonNode findWeatherDataForDate(JsonNode data, LocalDate targetDate) {
    for (JsonNode dayData : data.path("daily")) {
      LocalDate date = LocalDate.ofInstant(
          Instant.ofEpochSecond(dayData.path("dt").asLong()), ZoneId.systemDefault());
      if (date.equals(targetDate)) {
        return dayData;
      }
    }
    return null;
  }

  // 특정 timestamp에 해당하는 날씨 데이터를 찾는 메서드 (hourly 데이터 사용)
  private JsonNode findWeatherDataForTimestamp(JsonNode data, LocalDateTime timestamp) {
    JsonNode closestData = null;
    long minDifference = Long.MAX_VALUE;

    for (JsonNode hourData : data.path("hourly")) {
      LocalDateTime dateTime = LocalDateTime.ofInstant(
          Instant.ofEpochSecond(hourData.path("dt").asLong()), ZoneId.systemDefault());
      long difference = Math.abs(Duration.between(dateTime, timestamp).toMinutes());

      // 가장 가까운 데이터를 찾음 (1시간 이내로 허용)
      if (difference < minDifference && difference <= 60) {
        minDifference = difference;
        closestData = hourData;
      }
    }

    return closestData;
  }
}
