package com.example.reve.dto;

import lombok.*;

/*
장바구니에 추가 요청에 대한 데이터를 전달해주는 DTO임.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartAddRequestDTO {

  // 수량
  private int quantity;
  // 향수
  private Long perfumeId;
}
