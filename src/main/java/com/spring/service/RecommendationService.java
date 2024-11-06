package com.spring.service;


import com.spring.model.*;
import com.spring.model.social_entity.SocialUserEntity;
import com.spring.repository.UserRepository;
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
  private final UserRepository userRepository;
  private final SocialUserRepository socialUserRepository;


  @Transactional
  public void saveRecommendation(RecommendationDto recommendationDto) {
    Optional<HomeUser> user = userRepository.findByUserId(recommendationDto.userId());
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


  @Transactional
  public void saveRecommendationSocial(RecommendationSocialDto recommendationDto) {
    Optional<SocialUserEntity> socialuser = Optional.ofNullable(socialUserRepository.findByUsername(recommendationDto.username()));
    if (socialuser.isPresent()) {
      RecommendationSocial recommendation = new RecommendationSocial();
      recommendation.setUsername(socialuser.get());
      recommendation.setRecommendation(recommendationDto.recommendation());
      recommendation.setCreateDate(recommendationDto.createDate());
      recSocialRepository.save(recommendation);
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


  @Transactional
  public List<RecommendationSocialDto> getSocialRecommendations(String username) {
    return recSocialRepository.findByUsername_Username(username)
        .stream()
        .map(recommendation -> new RecommendationSocialDto(
            recommendation.getId(),
            recommendation.getUsername().getUsername(),
            recommendation.getRecommendation(),
            recommendation.getCreateDate()
        ))
        .collect(Collectors.toList());
  }

}
