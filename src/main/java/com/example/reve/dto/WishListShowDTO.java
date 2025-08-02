package com.example.reve.dto;

import lombok.Data;

@Data
public class WishListShowDTO {
  // 상품 이미지
  private String haverImageUrl;
  // 상품명
  private String perfumName;
  // 할인값
  private int discount;
  // 가격
  private int price;
  // 향종류
  private String scent;
  // 간략 설명
  private String desciptionTitle;
  // 별 평균
  private String ratingAvg;
  // 리뷰 수
  private int countReview;
}
