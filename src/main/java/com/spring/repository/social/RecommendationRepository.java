package com.spring.repository.social;


import com.spring.model.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;


public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
  List<Recommendation> findByUserId_UserId(String userId);
}
