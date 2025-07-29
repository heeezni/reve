package com.example.reve.dto;

import lombok.*;

/*
향수저장에 관한 데이터를 전달하기 위한 DTO임.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfumeSaveRequestDto {

  // 향수 이름
  private String perfumeName;
  // 향(예 : 플로럴, 머스크)
  private String scent;
  // 향수 설명
  private String description;
  // 향수 용량(예 : 70ml)
  private String volume;
  // 향수 가격
  private Integer price;
  // 향수 할인가
  private Integer discount;
  // 향수 재고
  private Integer stock;
  // 향수 원본 이미지
  private String imageUrl;
  // 향수 호버 이미지
  private String hoverImageUrl;
}
