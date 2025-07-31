package com.example.reve.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateProfileDTO {
  private String nickname;
  private String profileUrl;
  private String name;
  private String birthday;
  private String phone;

  UpdateProfileDTO(String profileUrl, String name, String birthday, String phone, String nickname) {
    this.profileUrl = profileUrl;
    this.name = name;
    this.birthday = birthday;
    this.phone = phone;
    this.nickname = nickname;
  }
}
