package com.example.reve.dto;

import lombok.Data;

/***
 * 회원 등록  DTO
 * -로그인 아이디
 * -비밀번호
 * -이름
 * -이메일
 * -전화번호
 */
@Data
public class CreateUserDTO {
  private String loginId;
  private String password;
  private String name;
  private String email;
  private String firstNum;
  private String middleNum;
  private String lastNum;
}
