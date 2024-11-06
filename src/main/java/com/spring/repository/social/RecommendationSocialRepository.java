package com.spring.repository.social;


import com.spring.model.RecommendationSocial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface RecommendationSocialRepository extends JpaRepository<RecommendationSocial, Long> {
  List<RecommendationSocial> findByUsername_Username(String username);
}
