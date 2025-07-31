package com.example.reve.dto;

import lombok.Data;

/***
 * 비밀번호 업데이트 DTO
 * password(기존), newPassword(변경)
 */
@Data
public class NewPasswordDTO {
  private String newPassword;
  private String password;
}
