package com.example.reve.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/shop")
public class ShopController {

  @GetMapping("/list")
  public String productList() {
    return "shop/list";
  }

  @GetMapping("/detail")
  public String productDetail() {
    return "shop/detail";
  }
}
