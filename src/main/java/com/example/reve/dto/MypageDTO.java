package com.example.reve.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MypageDTO {
  private String email;
  private LocalDateTime createdAt;
  private String profileUrl;

  public MypageDTO(String email, LocalDateTime createdAt, String profileUrl) {
    this.email = email;
    this.createdAt = createdAt;
    this.profileUrl = profileUrl;
  }
}
