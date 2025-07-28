package com.example.reve.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "perfume")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Perfume extends BaseEntity {

  // PK
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "perfume_id")
  private Long perfumeId;

  // 향수 이름
  @Column(name = "perfume_name", nullable = false)
  private String perfumeName;

  // 향수 설명
  @Column(nullable = false)
  private String description;

  // 향수 용량(예 : 70ml)
  @Column(nullable = false)
  private String volume;

  // 가격
  @Column(nullable = false)
  private Integer price;

  // 할인 가격
  @Column(nullable = false)
  private Integer discount;

  // 재고량
  @Column(nullable = false)
  private Integer stock;

  // 향수 이미지
  @Column(nullable = false)
  private String imageUrl;

  // 관계 설정
  // 향수와 리뷰와의 관계 (1 : N)
  @OneToMany(mappedBy = "perfume", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Builder.Default
  private List<Review> reviewList = new ArrayList<>();
}
