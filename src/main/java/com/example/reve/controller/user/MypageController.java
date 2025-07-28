package com.example.reve.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mypage")
public class MypageController {

  @GetMapping
  public String mypage() {
    return "user/mypage/index";
  }

  @GetMapping("/wishlist")
  public String wishlist() {
    return "user/mypage/wishlist";
  }

  @GetMapping("/order")
  public String mypageOrder() {
    return "user/mypage/order";
  }

  @GetMapping("/account")
  public String mypageAccount() {
    return "user/mypage/account";
  }
}
