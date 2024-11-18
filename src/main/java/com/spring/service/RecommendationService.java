package com.spring.service;


import com.spring.model.AllUser;
import com.spring.model.Recommendation;
import com.spring.model.RecommendationDto;
import com.spring.repository.AllUserRepository;
import com.spring.repository.social.RecommendationRepository;
import com.spring.repository.social.RecommendationSocialRepository;
import com.spring.repository.social.SocialUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class RecommendationService {
  private final RecommendationRepository recRepository;
  private final RecommendationSocialRepository recSocialRepository;
  private final AllUserRepository userRepository;
  private final SocialUserRepository socialUserRepository;


  @Transactional
  public void saveRecommendation(RecommendationDto recommendationDto) {
    Optional<AllUser> user = userRepository.findByUserId(recommendationDto.userId());
    if (user.isPresent()) {
      Recommendation recommendation = new Recommendation();
      recommendation.setUserId(user.get());
      recommendation.setRecStyle(recommendationDto.recStyle());
      recommendation.setRecActivity(recommendationDto.recActivity());
      recommendation.setTemp_high(recommendationDto.temp_high());
      recommendation.setTemp_low(recommendationDto.temp_low());
      recommendation.setCreateDate(recommendationDto.createDate());
      recRepository.save(recommendation);
    } else {
      throw new IllegalArgumentException("Recommendation cannot be null");
    }
  }

  //조회로직...
  @Transactional
  public List<RecommendationDto> getRecommendations(String userId) {
    return recRepository.findByUserId_UserId(userId)
        .stream()
        .map(recommendation -> new RecommendationDto(
            recommendation.getId(),
            recommendation.getUserId().getUserId(),
            recommendation.getRecStyle(),
            recommendation.getRecActivity(),
            recommendation.getTemp_high(),
            recommendation.getTemp_low(),
            recommendation.getCreateDate()
        ))
        .collect(Collectors.toList());
  }
}