package com.example.reve.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.*;

/*
리뷰 생성시 필요한 데이터를 전달해주는 DTO임.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCreateDTO {

  // 리뷰 내용
  private String content;
  // 리뷰 별점
  private Double rating;
  // 리뷰 이미지
  private MultipartFile imageUrl;
  // 향수
  private Long perfumeId;
}
