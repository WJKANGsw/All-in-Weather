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
  @JoinColumn(name = "userId",referencedColumnName = "userId", nullable = false)
  private AllUser userId;


  private String style;


}


