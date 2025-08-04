package com.example.reve.dto;

import java.time.LocalDateTime;

import com.example.reve.domain.Perfume;

import lombok.Data;

@Data
public class GetOrderDTO {
  private Long orderId;
  private String orderNumber;
  private LocalDateTime createAt;
  private Perfume perfume;
  private int totalPrice;
}
