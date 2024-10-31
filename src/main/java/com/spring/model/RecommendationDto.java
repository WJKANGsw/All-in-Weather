package com.spring.model;




import java.time.LocalDate;
import java.time.LocalDateTime;


public record RecommendationDto(int id, String userId, String recommendation, LocalDateTime createDate) {


}
