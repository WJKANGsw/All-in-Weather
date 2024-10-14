package com.spring.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "recommendation")
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private HomeUser user;

    @ManyToOne
    @JoinColumn(name = "weather_id", nullable = false)
    private Weather weather;

    @Column(nullable = false, length = 1)
    private String gender;

    @Column(nullable = false)
    private int age;

    @Column(name = "weather_comment", nullable = false, length = 50)
    private String weatherComment;

    @Column(nullable = false)
    private String recommendation;
}