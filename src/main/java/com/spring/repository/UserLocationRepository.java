package com.spring.repository;

import com.spring.model.UserLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLocationRepository extends JpaRepository<UserLocation, Long> {
  UserLocation findFirstByUserIdOrderByIdDesc(String userId);
}
