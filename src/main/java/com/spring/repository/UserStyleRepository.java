package com.spring.repository;

import com.spring.model.UserStyle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserStyleRepository extends JpaRepository<UserStyle, Long> {
  List<UserStyle> findByUserId_UserId(String userId);
}
