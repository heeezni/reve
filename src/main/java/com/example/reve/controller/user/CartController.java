package com.example.reve.controller.user;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.reve.dto.CartAddRequestDTO;
import com.example.reve.service.CartService;

import lombok.RequiredArgsConstructor;

/*
장바구니 관련 매핑을 처리해주는 컨트롤러임.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

  private final CartService cartService;

  // 장바구니에 데이터를 저장하는 매핑임.
  @PostMapping("/add")
  public ResponseEntity<?> addToCart(
      @RequestBody CartAddRequestDTO requestDto, Principal principal) {
    // Principal을 통해 로그인한 사용자의 loginId(email 또는 username)을 가져옴
    String loginId = principal.getName();

    int updatedCartItemCount =
        cartService.addToCart(loginId, requestDto.getPerfumeId(), requestDto.getQuantity());

    // 클라이언트에 장바구니 개수 응답
    return ResponseEntity.ok().body(Map.of("cartItemCount", updatedCartItemCount));
  }
}
