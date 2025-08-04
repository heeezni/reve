package com.example.reve.dto;

import java.time.LocalDateTime;

import com.example.reve.domain.Perfume;

import lombok.Data;

@Data
public class GetOrderDTO {
  private String orderNumber;
  private LocalDateTime createAt;
  private Perfume perfume;
  private int totalPrice;
}
