package com.example.reve.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Point extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "point_id")
  private Long pointId;

  // 적립금
  @Min(0)
  @Column(nullable = false)
  private Integer pointAmount;

  // 포인트랑 유저(N:1)
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // 포인트랑 주문(1:N)
  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "order_id")
  private Order order;
}
