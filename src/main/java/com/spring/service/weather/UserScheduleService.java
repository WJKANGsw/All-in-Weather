package com.spring.service.weather;

import com.spring.model.AllUser;
import com.spring.model.UserSchedule;
import com.spring.repository.AllUserRepository;
import com.spring.repository.UserScheduleRepository;
import com.spring.repository.WeatherDataRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserScheduleService {

  @Autowired
  private UserScheduleRepository userScheduleRepository;

  @Autowired
  private AllUserRepository allUserRepository;

  @Autowired
  private WeatherDataRepository weatherDataRepository;

  @Autowired
  private WeatherService weatherService;

  // 일정 추가
  public UserSchedule addSchedule(String userId, LocalDate date, String activity, String timeSlot, String placeName, String address, LocalDateTime timestamp, Double latitude, Double longitude) {
    // userId로 AllUser 객체 조회
    AllUser user = allUserRepository.findByUserId(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    boolean exists = userScheduleRepository.existsByUserIdAndDateAndTimeSlot(user, date, timeSlot);
    if (exists) {
      throw new IllegalArgumentException("An activity already exists for this date and time slot.");
    }

    // UserSchedule 객체 생성 및 설정
    UserSchedule schedule = new UserSchedule();
    schedule.setUserId(user);
    schedule.setDate(date);
    schedule.setActivity(activity);
    schedule.setTimeSlot(timeSlot);
    schedule.setPlaceName(placeName);
    schedule.setAddress(address);
    schedule.setTimestamp(timestamp);
    schedule.setLatitude(latitude);
    schedule.setLongitude(longitude);
    return userScheduleRepository.save(schedule);
  }

  // 일정 조회 (사용자별)
  public List<UserSchedule> getUserSchedules(String userId) {
    return userScheduleRepository.findByUserId_UserId(userId);
  }

  // 특정 날짜의 일정 조회
  public List<UserSchedule> getUserSchedulesByDate(String userId, LocalDate date) {
    return userScheduleRepository.findByUserId_UserIdAndDate(userId, date);
  }

  // 일정 삭제 메서드
  @Transactional
  public void deleteSchedule(String userId, Long scheduleId) {
    AllUser user = allUserRepository.findByUserId(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));
    UserSchedule schedule = userScheduleRepository.findByIdAndUserId(scheduleId, user);
    if (schedule != null) {
      userScheduleRepository.delete(schedule);
    } else {
      throw new IllegalArgumentException("Schedule not found for user: " + userId);
    }
    weatherDataRepository.deleteByUserIdAndTimestamp(userId, schedule.getTimestamp());
  }

  // 일정 수정 서비스 메서드
  @Transactional
  public UserSchedule updateSchedule(String userId, Long scheduleId, String newActivity, String timeSlot, String placeName, String address, Double newLatitude, Double newLongitude, LocalDateTime newTimestamp) {
    AllUser user = allUserRepository.findByUserId(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    UserSchedule schedule = userScheduleRepository.findByIdAndUserId(scheduleId, user);
    if (schedule == null) {
      throw new IllegalArgumentException("Schedule not found for user: " + userId);
    }

    LocalDateTime oldTimestamp = schedule.getTimestamp();
    Double oldLatitude = schedule.getLatitude();
    Double oldLongitude = schedule.getLongitude();

    System.out.println("Old Timestamp: " + oldTimestamp);
    System.out.println("New Timestamp: " + newTimestamp);
    System.out.println("Old Latitude: " + oldLatitude);
    System.out.println("Old Longitude: " + oldLongitude);
    System.out.println("New Latitude: " + newLatitude);
    System.out.println("New Longitude: " + newLongitude);

    schedule.setActivity(newActivity); // 새로운 활동으로 업데이트
    schedule.setTimeSlot(timeSlot);
    schedule.setPlaceName(placeName);
    schedule.setAddress(address);
    schedule.setLatitude(newLatitude);
    schedule.setLongitude(newLongitude);
    schedule.setTimestamp(newTimestamp);

    // 새로운 날씨 데이터 저장 및 기존 데이터 삭제
    if ((newTimestamp != null && !newTimestamp.isEqual(oldTimestamp)) ||
        (newLatitude != null && newLongitude != null &&
            (!newLatitude.equals(oldLatitude) || !newLongitude.equals(oldLongitude)))) {

      // 1. 기존 날씨 데이터 삭제
      if (oldTimestamp != null && oldLatitude != null && oldLongitude != null) {
        System.out.println("Deleting old weather data for userId: " + userId + " at timestamp: " + oldTimestamp);
        weatherService.deleteWeatherData(userId, oldLatitude, oldLongitude, oldTimestamp);
      }

      // 2. 새로운 날씨 데이터 저장 (기존 위도와 경도를 활용)
      if (newLatitude == null) {
        newLatitude = oldLatitude;
      }
      if (newLongitude == null) {
        newLongitude = oldLongitude;
      }
      if (newLatitude != null && newLongitude != null) {
        System.out.println("Saving new weather data for userId: " + userId + " at timestamp: " + newTimestamp);
        weatherService.saveWeatherData(userId, newLatitude, newLongitude, newTimestamp);
      } else {
        System.out.println("Latitude or longitude is null, skipping weather data save.");
      }
    } else {
      System.out.println("No changes in timestamp or location, skipping weather data update.");
    }


    return userScheduleRepository.save(schedule);
  }

  public List<UserSchedule> getSchedulesBetween(String userId, LocalDateTime start, LocalDateTime end) {
    return userScheduleRepository.findByUserId_UserIdAndTimestampBetween(userId, start, end);
  }
}
