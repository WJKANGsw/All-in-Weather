package com.spring.controller;


import com.spring.model.RecommendationDto;
import com.spring.model.RecommendationSocialDto;
import com.spring.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;


@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class RecommendationController {


  private final RecommendationService recommendationService;


  @PostMapping("/save")
  public ResponseEntity<String> saveResult(@RequestBody RecommendationDto recommendationDto) {
    recommendationService.saveRecommendation(recommendationDto);
    return ResponseEntity.ok("Chat result saved successfully");
  }

  @PostMapping("/social_save")
  public ResponseEntity<String> saveResult(@RequestBody RecommendationSocialDto recommendationSocialDto) {
    recommendationService.saveRecommendationSocial(recommendationSocialDto);
    return ResponseEntity.ok("Chat result saved successfully");
  }


  @GetMapping("/read/{userId}")
  public ResponseEntity<List<RecommendationDto>> getRedByUserId(@PathVariable String userId){
    List<RecommendationDto> recommendations = recommendationService.getRecommendations(userId);
    return ResponseEntity.ok(recommendations);
  }

  @GetMapping("/read/social/{username}")
  public ResponseEntity<List<RecommendationSocialDto>> getRedByUsername(@PathVariable String username){
    List<RecommendationSocialDto> recommendations = recommendationService.getSocialRecommendations(username);
    return ResponseEntity.ok(recommendations);
  }
}


