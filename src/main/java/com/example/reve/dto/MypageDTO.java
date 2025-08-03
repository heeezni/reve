package com.example.reve.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MypageDTO {
  private String email;
  private LocalDateTime createdAt;
  private String profileUrl;
  private String name;
  private Long userId;

  public MypageDTO(
      String email, LocalDateTime createdAt, String profileUrl, String name, Long userId) {
    this.email = email;
    this.createdAt = createdAt;
    this.profileUrl = profileUrl;
    this.name = name;
    this.userId = userId;
  }
}
