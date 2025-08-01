package com.example.reve.dto;

import lombok.*;

/*
장바구니 추가 응답에 대한 데이터를 전달해주는 DTO임.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartAddResponseDTO {

  // 장바구니 아이템 수량
  private int cartItemCount;
}
