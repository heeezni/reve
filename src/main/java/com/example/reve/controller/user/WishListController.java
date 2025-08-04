package com.example.reve.controller.user;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.reve.domain.User;
import com.example.reve.repository.UserRepository;
import com.example.reve.service.CartService;
import com.example.reve.service.WishListService;

import lombok.RequiredArgsConstructor;

/*
찜목록 url을 매핑시켜주는 컨트롤러임.
 */
@Controller
@RequiredArgsConstructor
public class WishListController {

  private final WishListService wishListService;
  private final UserRepository userRepository;
  private final CartService cartService;

  // 찜목록에 추가 버튼
  @PostMapping("/wishlist/toggle")
  public String toggleWish(@RequestParam Long perfumeId, Principal principal) {
    String loginId = principal.getName();
    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    wishListService.toggleWish(user, perfumeId);
    return "redirect:/shop/detail?id=" + perfumeId;
  }

  // 상품 목록에 있는 버튼
  @PostMapping("/wishlist/list")
  public String listWish(@RequestParam("perfumeId") Long perfumeId, Principal principal) {
    if (principal == null) {
      return "redirect:/login";
    }
    String loginId = principal.getName();
    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수 없음"));
    wishListService.toggleWish(user, perfumeId);
    return "redirect:/shop/list";
  }

  // 선택한 상품 삭제 버튼
  @PostMapping("/wishlist/delete")
  public String deleteWish(@RequestParam Long perfumeId, Principal principal) {

    if (principal == null) {
      return "redirect:/login";
    }
    String loginId = principal.getName();
    wishListService.wishperfumeDelete(perfumeId, loginId);
    return "redirect:/mypage/wishlist";
  }

  // 선택한 상품들 삭제 버튼
  @PostMapping("/wishlist/deleteList")
  public String deleteListWish(
      @RequestParam("perfumeIds") List<Long> perfurmList, Principal principal) {
    if (principal == null) {
      return "redirect:/login";
    }
    String loginId = principal.getName();
    wishListService.wishlistDelete(perfurmList, loginId);
    return "redirect:/mypage/wishlist";
  }

  // 장바구니 추가버튼
  @PostMapping("/wishlist/addCart")
  public String addCart(
      @RequestParam Long perfumeId, Principal principal, RedirectAttributes redirectAttributes) {
    if (principal == null) {
      return "redirect:/login";
    }
    String loginId = principal.getName();
    try {
      cartService.addToCart(loginId, perfumeId, 1);
      wishListService.wishperfumeDelete(perfumeId, loginId);
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "이미 장바구니에 있는 상품입니다");
      return "redirect:/mypage/wishlist";
    }
    return "redirect:/mypage/wishlist";
  }

  @PostMapping("/wishlist/toggle-ajax")
  @ResponseBody
  public ResponseEntity<String> toggleWishAjax(
      @RequestParam("perfumeId") Long perfumeId, Principal principal) {
    if (principal == null) {
      return ResponseEntity.status(401).body("로그인이 필요합니다.");
    }

    String loginId = principal.getName();
    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수 없습니다."));

    boolean wished = wishListService.toggleWish(user, perfumeId);
    return ResponseEntity.ok(wished ? "added" : "removed");
  }

  // 장바구니 추가
  @PostMapping("/wishlist/addCart-ajax")
  @ResponseBody
  public ResponseEntity addCart(@RequestParam Long perfumeId, Principal principal) {
    if (principal == null) {
      return ResponseEntity.status(401).body("unauthorized");
    }
    String loginId = principal.getName();
    boolean result = wishListService.addCart(perfumeId, loginId);
    if (result) {
      return ResponseEntity.ok("added");
    } else {
      return ResponseEntity.ok("exists");
    }
  }

  // 위시리스트 삭제
  @PostMapping("/wishlist/delete-ajax")
  @ResponseBody
  public ResponseEntity<String> deleteWishlist(@RequestParam Long perfumeId, Principal principal) {
    if (principal == null) {
      return ResponseEntity.status(401).body("unauthorized");
    }
    String loginId = principal.getName();
    wishListService.wishperfumeDelete(perfumeId, loginId);
    return ResponseEntity.ok("deleted");
  }
}
