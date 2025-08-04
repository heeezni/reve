package com.example.reve.controller.user;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.reve.domain.Cart;
import com.example.reve.domain.CustomUserDetails;
import com.example.reve.domain.Perfume;
import com.example.reve.domain.User;
import com.example.reve.dto.CartAddRequestDTO;
import com.example.reve.dto.CartFormDTO;
import com.example.reve.dto.CouponDTO;
import com.example.reve.service.CartService;
import com.example.reve.service.CouponService;
import com.example.reve.service.UserService;

import lombok.RequiredArgsConstructor;

/*
장바구니 관련 매핑을 처리해주는 컨트롤러임.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

  private final CartService cartService;
  private final CouponService couponService;
  private final UserService userService;

  @GetMapping
  public String showCartPage(Principal principal, Model model) {
    String loginId = principal.getName();
    List<Cart> cartList = cartService.getCartItemsByLoginId(loginId);
    model.addAttribute("cartItems", cartList);

    // 주문 요약용 데이터도 같이 계산해 넘겨줄 수 있음
    int totalPrice =
        cartList.stream().mapToInt(item -> item.getQuantity() * item.getPerfume().getPrice()).sum();
    model.addAttribute("totalPrice", totalPrice);

    List<Perfume> recommendedPerfumes = cartService.getRecommendedPerfumes(cartList);
    model.addAttribute("recommendedPerfumes", recommendedPerfumes);

    User user =
        userService.loadUserByUsername(loginId) instanceof CustomUserDetails cud
            ? cud.getUser()
            : null;

    if (user != null) {
      Long userId = user.getUserId();
      List<CouponDTO> coupons = couponService.getCoupon(userId);
      model.addAttribute("coupons", coupons);

      int userPoint = userService.getPointByUserId(userId);
      model.addAttribute("userPoint", userPoint);
    } else {
      model.addAttribute("coupons", List.of());
      model.addAttribute("userPoint", 0); // 포인트가 없으면 0처리
    }

    CartFormDTO form = new CartFormDTO(); // DTO는 new 해도 됨
    model.addAttribute("cartFormDTO", form);

    return "cart/cart";
  }

  // 장바구니에 데이터를 저장하는 매핑임.
  @PostMapping("/add")
  public ResponseEntity<?> addToCart(
      @RequestBody CartAddRequestDTO requestDto, Principal principal) {
    try {
      String loginId = principal.getName();

      int updatedCartItemCount =
          cartService.addToCart(loginId, requestDto.getPerfumeId(), requestDto.getQuantity());

      return ResponseEntity.ok().body(Map.of("cartItemCount", updatedCartItemCount));
    } catch (IllegalStateException e) {
      // 이미 장바구니에 담긴 상품입니다.
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    } catch (IllegalArgumentException e) {
      // 사용자 또는 상품이 존재하지 않을 경우
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  // MVC 방식용 즉, 동기방식 전체삭제
  @PostMapping("/deleteAll")
  public String deleteAllCartItems(Principal principal) {
    String loginId = principal.getName();
    cartService.removeAllCartItemsByLoginId(loginId);
    // 전체 삭제 후 장바구니 페이지로 리다이렉트
    return "redirect:/cart";
  }

  @PutMapping("/{cartId}/quantity")
  public ResponseEntity<?> updateQuantity(@PathVariable Long cartId, @RequestParam int quantity) {
    try {
      cartService.changeQuantity(cartId, quantity);
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  // 비동기방식용 삭제
  @DeleteMapping("/{cartId}")
  public ResponseEntity<?> deleteCartItem(@PathVariable Long cartId) {
    cartService.removeCartItem(cartId);
    return ResponseEntity.ok().build();
  }
}
