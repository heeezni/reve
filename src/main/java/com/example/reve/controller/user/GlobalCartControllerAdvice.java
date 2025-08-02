package com.example.reve.controller.user;

import java.security.Principal;
import java.util.List;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.reve.domain.Cart;
import com.example.reve.service.CartService;

import lombok.RequiredArgsConstructor;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalCartControllerAdvice {

  private final CartService cartService;

  @ModelAttribute
  public void addCartCountToModel(Model model, Principal principal) {
    if (principal != null) {
      String loginId = principal.getName();
      List<Cart> cartItems = cartService.getCartItemsByLoginId(loginId);
      int totalCount = cartItems.size();
      model.addAttribute("cartCount", totalCount);
    } else {
      // 비로그인 상태면 0으로 세팅하거나 생략 가능
      model.addAttribute("cartCount", 0);
    }
  }
}
