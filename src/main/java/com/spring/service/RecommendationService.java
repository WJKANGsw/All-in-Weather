package com.spring.service;


import com.spring.model.HomeUser;
import com.spring.model.Recommendation;
import com.spring.model.RecommendationDto;
import com.spring.repository.UserRepository;
import com.spring.repository.social.RecommendationRepository;
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
  private final UserRepository userRepository;


  @Transactional
  public void saveRecommendation(RecommendationDto recommendationDto) {
    Optional<HomeUser> user = userRepository.findByUserId(recommendationDto.userId());
    if (user.isPresent()) {
      Recommendation recommendation = new Recommendation();
      recommendation.setUserId(user.get());
      recommendation.setRecommendation(recommendationDto.recommendation());
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
            recommendation.getRecommendation(),
            recommendation.getCreateDate()
        ))
        .collect(Collectors.toList());
  }
}
