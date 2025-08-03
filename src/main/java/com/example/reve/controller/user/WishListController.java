package com.example.reve.controller.user;

import java.security.Principal;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.reve.domain.User;
import com.example.reve.repository.UserRepository;
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
  //선택한 상품 삭제 버튼
  @PostMapping("/wishlist/delete")
  public void deleteWish(@RequestParam Long perfumeId, Principal principal) {

  }
  //선택한 상품즐 삭제 버튼
  @PostMapping("/wishlist/deleteList")
  public void deleteListWish(@RequestParam Long perfumeId, Principal principal) {

  }
  //장바구니 추가버튼
  @PostMapping("/wishlist/addCart")
  public void addCart(@RequestParam Long perfumeId, Principal principal) {

  }
}
