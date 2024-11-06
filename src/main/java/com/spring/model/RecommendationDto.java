package com.spring.model;




import java.time.LocalDate;
import java.time.LocalDateTime;


public record RecommendationDto(int id, String userId,String recStyle, String recActivity, String temp_high, String temp_low, LocalDateTime createDate) {

}
