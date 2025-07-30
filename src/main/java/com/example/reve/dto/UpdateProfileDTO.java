package com.example.reve.dto;

import lombok.Data;

@Data
public class UpdateProfileDTO {
  private String loginId;
  private String profileUrl;
  private String name;
  private String nickname;
  private String birthday;
  private String phone;
}
