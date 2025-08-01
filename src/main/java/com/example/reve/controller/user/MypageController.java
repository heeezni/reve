package com.example.reve.controller.user;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.reve.domain.CustomUserDetails;
import com.example.reve.dto.CouponDTO;
import com.example.reve.dto.NewPasswordDTO;
import com.example.reve.dto.UpdateProfileDTO;
import com.example.reve.dto.WishlistDTO;
import com.example.reve.service.CouponService;
import com.example.reve.service.UserService;
import com.example.reve.service.WishlistService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
@Slf4j
public class MypageController {

  private final UserService userService;
  private final CouponService couponService;
  private final WishlistService wishlistService;

  @GetMapping
  public String mypage(Model model, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    String loginId = customUserDetails.getUsername();
    if (loginId != null) {
      model.addAttribute("mypage", userService.selectMypage(loginId));
      return "user/mypage/index";
    } else {
      log.error("loginId is {}", loginId);
      return "redirect:/";
    }
  }

  @GetMapping("/order")
  public String mypageOrder() {
    return "user/mypage/order";
  }

  // 찜 목록
  @GetMapping("/wishlist")
  public String wishlist() {
    return "user/mypage/wishlist";
  }

  // 찜 목록 추가
  @PostMapping("/wishlist/add")
  public String wishlistAdd(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      WishlistDTO wishlistDTO,
      HttpServletRequest request) {
    Long userId = customUserDetails.getUser().getUserId();
    Long performId = wishlistDTO.getPerfume().getPerfumeId();
    // 목록 추가하기 요청
    wishlistService.addWishlist(userId, performId);
    // 현재페이지로 돌아가기
    String thisPage = request.getHeader("Referer");
    return "redirect:" + thisPage;
  }

  // 쿠폰 목록
  @GetMapping("/coupons")
  public String cupons(Model model, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    // 특정 회원 찾기
    Long userId = customUserDetails.getUser().getUserId();
    List<CouponDTO> couponList = couponService.getCoupon(userId);
    model.addAttribute("couponList", couponList);
    log.info("회원이 가지고 있는 쿠폰 리스트 couponList {}", model.addAttribute("couponList", couponList));

    return "user/mypage/coupons";
  }

  @GetMapping("/account")
  public String mypageAccount(
      @AuthenticationPrincipal CustomUserDetails customUserDetails, Model model) {
    String loginId = customUserDetails.getUsername();
    // 프로필에 필요한 정보 먼저 조회
    model.addAttribute("profile", userService.profileById(loginId));
    return "user/mypage/account";
  }

  /***
   * 회원 정보 중 기본정보 변경하는 컨트롤러
   * (이름,닉네임, 생일, 휴대폰 번호 변경)
   * @param updateProfileDTO 회원의 기본정보
   * @return "/user/mypage/account"
   */
  @PostMapping("/account/profile")
  public String updateProfile(
      UpdateProfileDTO updateProfileDTO,
      Model model,
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    log.info("회원 정보 수정 컨트롤러 호출");
    try {
      // 수정된 회원 정보 가져오기
      String loginId = customUserDetails.getUsername();
      model.addAttribute("profile", userService.update(updateProfileDTO, loginId));
      log.info("회원 정보 수정 성공 : {}", userService.update(updateProfileDTO, loginId).toString());
    } catch (Exception e) {
      log.error("회원 정보 수정 실패");
      throw new RuntimeException(e);
    }
    return "redirect:/mypage/account";
  }

  /***
   * 비밀번호 변경 컨트롤러
   * 변경 시 자동으로 로그인 아웃
   * @param newPasswordDTO 비밀번호 변경 DTO
   * @param session 로그인 아웃을 위한 session
   * @return "redirect:/" 성공 시 메인페이지로 이동
   */
  @PostMapping("/account/password")
  public String updatePassword(
      NewPasswordDTO newPasswordDTO,
      HttpSession session,
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    log.info("비밀번호 변경 컨트롤러 호출");
    String loginId = customUserDetails.getUsername();
    boolean result = userService.checkPassword(newPasswordDTO, loginId);
    if (result) {
      boolean result2 = userService.updatePassword(newPasswordDTO, loginId);
      if (result2) {
        session.setAttribute("result2", true);
        // 세션 초기화(로그인 아웃)
        session.invalidate();
        return "redirect:/";
      }
    } else {
      log.error("비밀번호 변경 실패");
      session.setAttribute("result", false);
    }
    return "redirect:/mypage/account";
  }
}
