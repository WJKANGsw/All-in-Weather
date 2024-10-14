package com.spring.repository;

import com.spring.model.HomeUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<HomeUser, Long> {
    HomeUser findByUsername(String username);
    boolean existsByEmail(String email);
}
