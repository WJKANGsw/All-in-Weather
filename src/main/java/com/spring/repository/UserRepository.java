package com.spring.repository;

import com.spring.model.HomeUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<HomeUser, Long> {
    HomeUser findByUsername(String username);
    boolean existsByUserId(String userId);

    Optional<HomeUser> findByUserId(String userId);
}
