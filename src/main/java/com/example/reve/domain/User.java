package com.example.reve.domain;

import jakarta.persistence.*;

import lombok.*;

/*
관리자, 유저 중 한 개의 역할을 가지고 있는 엔터티임.
 */
@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

  // pk
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Long userId;

  // 유저 이름
  private String name;

  // 전화번호
  private String phone;

  // 이메일
  private String email;

  // 생일
  private String birthday;

  // 프로필 사진
  private String profileUrl;

  // 닉네임
  private String nickname;

  // 로그인 아이디
  @Column(name = "login_id")
  private String loginId;

  // 비밀번호
  private String password;

  // 역할
  @Builder.Default
  @Enumerated(EnumType.STRING)
  private Role role = Role.USER;
}
