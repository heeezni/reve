package com.example.reve.dto;

import lombok.Data;

@Data
public class CartItem {
  private String name;
  private String image;
  private Integer quantity;
  private Integer pricePerItem;
  private Integer totalPrice;
}
