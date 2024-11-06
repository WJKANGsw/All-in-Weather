package com.spring.model;

import com.spring.model.social_entity.SocialUserEntity;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "recommendation_social")
public class RecommendationSocial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

//    @ManyToOne // Recommendation 레코드가 하나의 HomeUser 레코드에 연결
//    @JoinColumn(name = "userid",referencedColumnName = "userId", nullable = false)  // userId null 값을 가질 수 없음
//    private HomeUser userId;

    @ManyToOne
    @JoinColumn(name = "username",referencedColumnName = "username", nullable = false)
    private SocialUserEntity username;

    @Column(nullable = false)
    private String recommendation;

    //날짜
    @CreationTimestamp
    @Column
    @DateTimeFormat(pattern = "yyyy-MM-dd/HH:mm")
    private LocalDateTime createDate;
}