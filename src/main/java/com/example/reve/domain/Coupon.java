package com.example.reve.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;

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

  // 쿠폰명
  @Enumerated(EnumType.STRING)
  private CouponName couponName;

  // 쿠폰 유효 시작일
  @Column(nullable = false)
  private LocalDate validFrom;

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
  @Column
  private LocalDateTime isUsedAt;

  // 관계 설정
  // 쿠폰과 유저와의 관계 (N : 1)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;
}
