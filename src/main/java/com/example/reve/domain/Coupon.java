package com.example.reve.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "coupon_id")
  private Long couponId;
  // 쿠폰 코드
  @Column(nullable = false)
  private String code;
  // 할인율
  @Column(nullable = false)
  private int discountRate;
  // 만료일
  @Column(nullable = false)
  private LocalDateTime expiresAt;
  // 사용 여부
  @Column(nullable = false)
  private Boolean isUsed;
  // 사용일
  @Column(nullable = true)
  private LocalDateTime isUsedAt;

  // 관계 설정
  // 쿠폰과 유저와의 관계 (N : 1)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = true)
  private User user;
}
