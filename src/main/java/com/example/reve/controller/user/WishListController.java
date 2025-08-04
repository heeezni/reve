package com.example.reve.controller.user;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.reve.domain.Perfume;
import com.example.reve.domain.User;
import com.example.reve.dto.CartItem;
import com.example.reve.repository.PerfumeRepository;
import com.example.reve.repository.UserRepository;
import com.example.reve.service.CartService;
import com.example.reve.service.WishListService;
import com.fasterxml.jackson.databind.ObjectMapper;

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
  private final PerfumeRepository perfumeRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();

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

  // 바로구매 기능
  @GetMapping("/wishlist/buyNow")
  public String buyNow(
      @RequestParam Long perfumeId,
      @RequestParam(defaultValue = "1") int quantity,
      Principal principal,
      Model model) {

    if (principal == null) {
      return "redirect:/login";
    }

    try {
      // 상품 정보 조회
      Perfume perfume =
          perfumeRepository
              .findById(perfumeId)
              .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

      // 디버깅: 향수 정보 출력
      System.out.println("=== 향수 정보 디버깅 (buyNow) ===");
      System.out.println("향수 ID: " + perfume.getPerfumeId());
      System.out.println("향수명: " + perfume.getPerfumeName());
      System.out.println("용량: " + perfume.getVolume());
      System.out.println("원가: " + perfume.getPrice());
      System.out.println("할인가: " + perfume.getDiscount());
      System.out.println("수량: " + quantity);
      System.out.println("========================");

      // CartItem 객체 생성
      CartItem cartItem = new CartItem();
      cartItem.setId(perfume.getPerfumeId());
      cartItem.setName(perfume.getPerfumeName() + " " + perfume.getVolume());
      cartItem.setImage(perfume.getHoverImageUrl());

      // 할인가가 0이면 원가를 사용, 아니면 원가에서 할인가를 뺀 금액을 사용
      int finalPrice =
          perfume.getDiscount() > 0
              ? (perfume.getPrice() - perfume.getDiscount())
              : perfume.getPrice();
      cartItem.setPricePerItem(finalPrice);
      cartItem.setQuantity(quantity);
      cartItem.setTotalPrice(finalPrice * quantity);

      // JSON으로 변환
      String itemsJson = objectMapper.writeValueAsString(List.of(cartItem));

      // 배송비 계산 (기본 3000원, 5만원 이상 무료)
      int deliveryFee = (finalPrice * quantity >= 50000) ? 0 : 3000;

      // 총 결제금액
      int totalAmount = (finalPrice * quantity) + deliveryFee;

      // 결제 페이지로 리다이렉트
      return "redirect:/order/checkout?items="
          + java.net.URLEncoder.encode(itemsJson, "UTF-8")
          + "&totalAmount="
          + totalAmount
          + "&deliveryFee="
          + deliveryFee
          + "&discountAmount=0"
          + "&itemCount="
          + quantity;

    } catch (Exception e) {
      // 오류 발생 시 위시리스트로 리다이렉트
      return "redirect:/mypage/wishlist?error=구매 처리 중 오류가 발생했습니다.";
    }
  }

  // 바로구매 후 위시리스트에서 삭제
  @PostMapping("/wishlist/buyNow-ajax")
  @ResponseBody
  public ResponseEntity<String> buyNowAjax(
      @RequestParam Long perfumeId,
      @RequestParam(defaultValue = "1") int quantity,
      Principal principal) {

    if (principal == null) {
      return ResponseEntity.status(401).body("unauthorized");
    }

    try {
      // 상품 정보 조회
      Perfume perfume =
          perfumeRepository
              .findById(perfumeId)
              .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

      // 디버깅: 향수 정보 출력
      System.out.println("=== 향수 정보 디버깅 (buyNowAjax) ===");
      System.out.println("향수 ID: " + perfume.getPerfumeId());
      System.out.println("향수명: " + perfume.getPerfumeName());
      System.out.println("용량: " + perfume.getVolume());
      System.out.println("원가: " + perfume.getPrice());
      System.out.println("할인가: " + perfume.getDiscount());
      System.out.println("수량: " + quantity);

      // 다른 향수들의 가격도 확인
      System.out.println("=== 다른 향수들 가격 확인 ===");
      List<Perfume> allPerfumes = perfumeRepository.findAll();
      for (Perfume p : allPerfumes) {
        System.out.println(
            "ID: "
                + p.getPerfumeId()
                + ", 이름: "
                + p.getPerfumeName()
                + ", 원가: "
                + p.getPrice()
                + ", 할인가: "
                + p.getDiscount());
      }
      System.out.println("========================");

      // CartItem 객체 생성
      CartItem cartItem = new CartItem();
      cartItem.setId(perfume.getPerfumeId());
      cartItem.setName(perfume.getPerfumeName() + " " + perfume.getVolume());
      cartItem.setImage(perfume.getHoverImageUrl());

      // 할인가가 0이면 원가를 사용, 아니면 원가에서 할인가를 뺀 금액을 사용
      int finalPrice =
          perfume.getDiscount() > 0
              ? (perfume.getPrice() - perfume.getDiscount())
              : perfume.getPrice();
      cartItem.setPricePerItem(finalPrice);
      cartItem.setQuantity(quantity);
      cartItem.setTotalPrice(finalPrice * quantity);

      // JSON으로 변환
      String itemsJson = objectMapper.writeValueAsString(List.of(cartItem));

      // 배송비 계산
      int deliveryFee = (finalPrice * quantity >= 50000) ? 0 : 3000;

      // 총 결제금액
      int totalAmount = (finalPrice * quantity) + deliveryFee;

      // 성공 응답과 함께 결제 정보 반환
      String response =
          String.format(
              "success:%s:%d:%d:%d:%d",
              java.net.URLEncoder.encode(itemsJson, "UTF-8"),
              totalAmount,
              deliveryFee,
              0,
              quantity);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      return ResponseEntity.status(500).body("error:" + e.getMessage());
    }
  }

  // 임시: 향수 가격 수정 (테스트용)
  @GetMapping("/wishlist/fix-price")
  @ResponseBody
  public ResponseEntity<String> fixPrice() {
    try {
      // 향수 ID 2의 할인가를 8000원으로 수정 (더 현실적인 가격)
      Perfume perfume = perfumeRepository.findById(2L).orElse(null);
      if (perfume != null) {
        perfume.setDiscount(8000);
        perfumeRepository.save(perfume);
        return ResponseEntity.ok("테스트향의 가격이 8,000원으로 수정되었습니다.");
      } else {
        return ResponseEntity.ok("향수를 찾을 수 없습니다.");
      }
    } catch (Exception e) {
      return ResponseEntity.status(500).body("오류: " + e.getMessage());
    }
  }
}
