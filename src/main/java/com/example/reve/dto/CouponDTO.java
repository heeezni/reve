package com.example.reve.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.reve.domain.CouponName;

import lombok.Data;

@Data
public class CouponDTO {

  // PK
  private Long couponId;
  // 쿠폰명
  private CouponName couponName;
  // 만료일
  private LocalDateTime expiresAt;
  // 사용여부
  private Boolean isUsed;
  // 쿠폰 유효 시작일
  private LocalDate validFrom;
}
