package com.example.reve.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class CouponByBirthdayDTO {

  // 쿠폰 코드
  private String code;
  // 쿠폰명
  private String couponName;
  // 할인율
  private int discountRate;
  // 만료일
  private LocalDate expiresAt;
  // 사용여부
  private Boolean isUsed;
  // 발급날짜
  private LocalDate issuedAt;
  // 쿠폰 유효 시작일
  private LocalDate validFrom;
}
