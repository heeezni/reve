package com.example.reve.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MypageDTO {
  private String email;
  private LocalDateTime createdAt;

  public MypageDTO(String email, LocalDateTime createdAt) {
    this.email = email;
    this.createdAt = createdAt;
  }
}
