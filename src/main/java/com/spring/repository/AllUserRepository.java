package com.spring.repository;

import com.spring.model.AllUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AllUserRepository extends JpaRepository<AllUser, Long> {
  Optional<AllUser> findByUserId(String userId);
  boolean existsByUserId(String userId);
  boolean existsByEmail(String email);
}