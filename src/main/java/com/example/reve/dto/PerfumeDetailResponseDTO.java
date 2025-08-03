package com.example.reve.dto;

import java.util.List;

import com.example.reve.domain.Perfume;
import com.example.reve.domain.Review;

import lombok.*;

/*
향수 상세 페이지에 대한 응답 데이터를 전달하는 DTO임.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfumeDetailResponseDTO {

  // PK
  private Long perfumeId;
  // 향수 이름
  private String perfumeName;
  // 향(예 : 플로럴, 머스크)
  private String scent;
  // 향수 한 줄 소개
  private String descriptionTitle;
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

  // 평균 평점
  private Double averageRating;
  // 리뷰 총 카운트
  private Integer reviewCount;

  // 향수에 대한 정보를 빌더에 저장함.
  public static PerfumeDetailResponseDTO fromEntity(Perfume perfume) {
    // 리뷰에 관한 로직임.
    List<Review> reviewList = perfume.getReviewList();
    int count = reviewList.size();
    double avgRating = 0.0;

    if (count > 0) {
      double sum = reviewList.stream().mapToDouble(Review::getRating).sum();
      avgRating = Math.round((sum / count) * 10) / 10.0; // 소수점 1자리 반올림
    }

    return PerfumeDetailResponseDTO.builder()
        .perfumeId(perfume.getPerfumeId())
        .perfumeName(perfume.getPerfumeName())
        .scent(perfume.getScent())
        .descriptionTitle(perfume.getDescriptionTitle())
        .description(perfume.getDescription())
        .volume(perfume.getVolume())
        .price(perfume.getPrice())
        .discount(perfume.getDiscount())
        .stock(perfume.getStock())
        .imageUrl(perfume.getImageUrl())
        .hoverImageUrl(perfume.getHoverImageUrl())
        .averageRating(avgRating)
        .reviewCount(count)
        .build();
  }
}
