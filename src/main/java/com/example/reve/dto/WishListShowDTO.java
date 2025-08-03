package com.example.reve.dto;

import lombok.Data;

@Data
public class WishListShowDTO {
  // 상품 아이디
  private Long perfumeId;
  // 상품 이미지
  private String hoverImageUrl;
  // 상품명
  private String perfumeName;
  // 할인값
  private int discount;
  // 가격
  private int price;
  // 향종류
  private String scent;
  // 간략 설명
  private String desciptionTitle;
  // 용량
  private String volume;
  // 별 평균
  private double ratingAvg;
  // 리뷰 수
  private int countReview;
}
