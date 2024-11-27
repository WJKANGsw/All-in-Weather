package com.spring.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_location", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"userId", "latitude", "longitude"})
})
@Data
public class UserLocation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String userId;
  private double latitude;
  private double longitude;

  // Getters and setters
}
