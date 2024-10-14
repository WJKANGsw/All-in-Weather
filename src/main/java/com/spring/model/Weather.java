package com.spring.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "weather")
public class Weather {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false)
    private java.sql.Date date;

    @Column(nullable = false)
    private float temp;

    @Column(nullable = false, length = 50)
    private String weather;

    @Column(name = "weather_desc", nullable = false, length = 255)
    private String weatherDesc;

    @Column(nullable = false)
    private float windSpeed;
}
