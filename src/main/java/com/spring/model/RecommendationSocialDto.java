package com.spring.model;




import java.time.LocalDateTime;


public record RecommendationSocialDto(int id, String username, String recommendation, LocalDateTime createDate) {


}
