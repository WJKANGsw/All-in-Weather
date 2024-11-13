package com.spring.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class UserStyle {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private AllUser user;

  private String style;

}

