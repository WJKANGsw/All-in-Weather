package com.spring.repository;

import com.spring.model.AllUser;
import com.spring.model.UserSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserScheduleRepository extends JpaRepository<UserSchedule, Long> {
  List<UserSchedule> findByUserId_UserId(String userId);
  List<UserSchedule> findByUserId_UserIdAndDate(String userId, LocalDate date);
  UserSchedule findByIdAndUserId(Long id, AllUser user);
  // userId와 특정 기간 사이의 일정 조회 (start와 end 사이의 일정)
  List<UserSchedule> findByUserId_UserIdAndTimestampBetween(String userId, LocalDateTime start, LocalDateTime end);
  @Modifying
  @Query("DELETE FROM UserSchedule us WHERE us.userId.userId = :userId")
  void deleteByUserId(@Param("userId") String userId);
}
