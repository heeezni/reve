package com.example.reve.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartFormDTO {

  // 선택된 쿠폰
  private String selectedCoupon;
  // 포인트 사용량
  private Integer usedPoints;
}
