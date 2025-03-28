package com.spring.service.weather;

import com.spring.model.AllUser;
import com.spring.model.UserSchedule;
import com.spring.repository.AllUserRepository;
import com.spring.repository.UserScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserScheduleService {

  @Autowired
  private UserScheduleRepository userScheduleRepository;

  @Autowired
  private AllUserRepository allUserRepository;

  // 일정 추가
  public UserSchedule addSchedule(String userId, LocalDate date, String activity, String timeSlot, LocalDateTime timestamp) {
    // userId로 AllUser 객체 조회
    AllUser user = allUserRepository.findByUserId(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    // UserSchedule 객체 생성 및 설정
    UserSchedule schedule = new UserSchedule();
    schedule.setUserId(user);
    schedule.setDate(date);
    schedule.setActivity(activity);
    schedule.setTimeSlot(timeSlot);
    schedule.setTimestamp(timestamp);
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
  public void deleteSchedule(String userId, Long scheduleId) {
    AllUser user = allUserRepository.findByUserId(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));
    UserSchedule schedule = userScheduleRepository.findByIdAndUserId(scheduleId, user);
    if (schedule != null) {
      userScheduleRepository.delete(schedule);
    } else {
      throw new IllegalArgumentException("Schedule not found for user: " + userId);
    }
  }

  // 일정 수정 서비스 메서드
  public UserSchedule updateSchedule(String userId, Long scheduleId, String newActivity) {
    AllUser user = allUserRepository.findByUserId(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    UserSchedule schedule = userScheduleRepository.findByIdAndUserId(scheduleId, user);
    if (schedule == null) {
      throw new IllegalArgumentException("Schedule not found for user: " + userId);
    }

    schedule.setActivity(newActivity); // 새로운 활동으로 업데이트
    return userScheduleRepository.save(schedule);
  }

  public List<UserSchedule> getSchedulesBetween(String userId, LocalDateTime start, LocalDateTime end) {
    return userScheduleRepository.findByUserId_UserIdAndTimestampBetween(userId, start, end);
  }
}
