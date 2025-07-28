package com.example.reve.domain;

import java.util.List;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "`order`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "order_id")
  private Long orderId;

  // 주문 총 금액
  @Column(nullable = false)
  private Integer totalPrice;

  // 주문 상태
  @Column(nullable = false)
  private String orderStatus;

  // 결제카드회사
  @Column(nullable = false)
  private String card;

  // 이메일
  @Column(nullable = false)
  private String email;

  // 수령인
  @Column(nullable = false)
  private String receiver;

  // 우편번호
  @Column(nullable = false)
  private String zipCode;

  // 주소
  @Column(nullable = false)
  private String address;

  // 전화번호
  @Column(nullable = false)
  private String tel;

  // 배송요청사항
  @Column(nullable = true)
  private String orderRequest;

  // 배송비밀번호
  @Column(nullable = false)
  private String orderPassword;

  // 관계 설정
  // 주문과 유저와의 관계 (N : 1)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  // 주문과 쿠폰과의 관계 (N : 1)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "coupon_id", nullable = true)
  private Coupon coupon;

  // 주문과 상세주문과의 관계 (1 : N)
  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItem> orderItems;
}
