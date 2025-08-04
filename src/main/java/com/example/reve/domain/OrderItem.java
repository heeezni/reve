package com.example.reve.domain;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "order_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "order_item_id")
  private Long orderItemId;

  // 상품명
  @Column(name = "product_name", nullable = false)
  private String productName;

  // 상품 이미지
  @Column(name = "product_image")
  private String productImage;

  // 주문수량
  @Column(name = "order_quantity", nullable = false)
  private Integer orderQuantity;

  // 개당 가격
  @Column(name = "price_per_item", nullable = false)
  private Integer pricePerItem;

  // 총 가격
  @Column(name = "total_price", nullable = false)
  private Integer totalPrice;

  // 관계 설정
  // 상세주문과 향수와의 관계 (N : 1) - 선택적 관계로 변경
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "perfume_id")
  private Perfume perfume;

  // 상세주문과 주문과의 관계 (N : 1)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;
}
