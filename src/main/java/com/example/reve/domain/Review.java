package com.example.reve.domain;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

  // PK
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "review_id")
  private Long reviewId;

  // 리뷰 내용
  @Column(nullable = false, length = 1000)
  private String content;

  // 별점
  @Column(nullable = false)
  private Double rating;

  // 리뷰 이미지 URL
  @Column(name = "image_url")
  private String imageUrl;

  // 관계 설정
  // 유저와의 관게 (N : 1)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // 향수와의 관계 (N : 1)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "perfume_id", nullable = false)
  private Perfume perfume;
}
