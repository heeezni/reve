package com.example.reve.controller.user;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.reve.domain.CustomUserDetails;
import com.example.reve.domain.Order;
import com.example.reve.domain.OrderItem;
import com.example.reve.dto.*;
import com.example.reve.service.CouponService;
import com.example.reve.service.OrderService;
import com.example.reve.service.PointService;
import com.example.reve.service.UserService;
import com.example.reve.service.WishListService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MypageController {

  private final UserService userService;
  private final CouponService couponService;
  private final WishListService wishListService;
  private final PointService pointService;
  private final OrderService orderService;

  @GetMapping
  public String mypage(Model model, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    String loginId = customUserDetails.getUsername();
    if (loginId != null) {
      model.addAttribute("mypage", userService.selectMypage(loginId));
      model.addAttribute("wish", wishListService.getCount(loginId));
      model.addAttribute("point", pointService.getPointAmount(loginId));

      List<GetOrderDTO> orderList = orderService.getAllOrders(loginId);
      model.addAttribute("orderList", orderList);

      int result = couponService.countCoupon(loginId);
      model.addAttribute("countCoupon", result);
      // mypage.userId == currentUserId 조건으로 수정/삭제 버튼 렌더링
      model.addAttribute("currentUserId", customUserDetails.getUser().getUserId());
      return "user/mypage/index";
    } else {
      return "redirect:/";
    }
  }

  @GetMapping("/order")
  public String mypageOrder(
      @AuthenticationPrincipal CustomUserDetails customUserDetails, Model model) {
    try {
      // 사용자 ID 가져오기 (임시로 1L 사용, 실제로는 로그인된 사용자 ID 사용)
      Long userId = customUserDetails.getUser().getUserId();

      // 사용자의 주문 내역 조회
      List<Order> userOrders = orderService.findOrdersByUserId(userId);

      // 주문 통계 정보
      Long todayOrderCount = orderService.getTodayOrderCount();
      Long thisMonthOrderCount = orderService.getThisMonthOrderCount();
      List<Object[]> orderStatusCount = orderService.getOrderStatusCount();

      // Model에 데이터 추가
      model.addAttribute("orders", userOrders);
      model.addAttribute("todayOrderCount", todayOrderCount);
      model.addAttribute("thisMonthOrderCount", thisMonthOrderCount);
      model.addAttribute("orderStatusCount", orderStatusCount);

      // 주문 상태별 개수 계산
      long orderedCount = 0, preparingCount = 0, shippingCount = 0, deliveredCount = 0;
      for (Object[] statusCount : orderStatusCount) {
        String status = (String) statusCount[0];
        Long count = (Long) statusCount[1];
        switch (status) {
          case "ORDERED":
            orderedCount = count;
            break;
          case "PREPARING":
            preparingCount = count;
            break;
          case "SHIPPING":
            shippingCount = count;
            break;
          case "DELIVERED":
            deliveredCount = count;
            break;
        }
      }

      model.addAttribute("orderedCount", orderedCount);
      model.addAttribute("preparingCount", preparingCount);
      model.addAttribute("shippingCount", shippingCount);
      model.addAttribute("deliveredCount", deliveredCount);

    } catch (Exception e) {
      // 오류 발생 시 빈 리스트로 초기화
      model.addAttribute("orders", new ArrayList<>());
      model.addAttribute("todayOrderCount", 0L);
      model.addAttribute("thisMonthOrderCount", 0L);
      model.addAttribute("orderedCount", 0L);
      model.addAttribute("preparingCount", 0L);
      model.addAttribute("shippingCount", 0L);
      model.addAttribute("deliveredCount", 0L);
    }

    return "user/mypage/order";
  }

  /** 주문 상세 페이지 */
  @GetMapping("/order-detail/{orderId}")
  public String orderDetail(
      @PathVariable Long orderId,
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      Model model) {
    try {
      // 사용자 ID 가져오기
      Long userId = customUserDetails.getUser().getUserId();

      // 주문 상세 정보 조회
      Optional<Order> orderOpt = orderService.findOrderById(orderId);
      if (orderOpt.isPresent()) {
        Order order = orderOpt.get();

        // 주문이 해당 사용자의 것인지 확인
        if (order.getUser() != null && order.getUser().getUserId().equals(userId)) {
          // 주문 상품 목록 조회
          List<OrderItem> orderItems = orderService.getOrderItemsByOrderId(orderId);

          // Model에 데이터 추가
          model.addAttribute("order", order);
          model.addAttribute("orderItems", orderItems);

          // 배송 예정일 계산
          LocalDateTime orderDate = order.getCreatedAt();
          LocalDateTime deliveryStart = orderDate.plusDays(1);
          LocalDateTime deliveryEnd = orderDate.plusDays(2);
          String deliveryDate =
              deliveryStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                  + " ~ "
                  + deliveryEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
          model.addAttribute("deliveryDate", deliveryDate);

        } else {
          // 권한이 없는 경우
          model.addAttribute("error", "해당 주문에 대한 접근 권한이 없습니다.");
          return "common/error";
        }
      } else {
        // 주문을 찾을 수 없는 경우
        model.addAttribute("error", "주문을 찾을 수 없습니다.");
        return "common/error";
      }

    } catch (Exception e) {
      model.addAttribute("error", "주문 상세 정보를 불러오는 중 오류가 발생했습니다.");
      return "common/error";
    }

    return "user/mypage/order-detail";
  }

  // 찜 목록
  @GetMapping("/wishlist")
  public String wishlist(Principal principal, Model model) {
    String loginId = principal.getName();
    List<WishListShowDTO> getWishlist = wishListService.getWishList(loginId);
    if (getWishlist == null) getWishlist = new ArrayList<>();
    model.addAttribute("wishlist", getWishlist);
    return "user/mypage/wishlist";
  }

  // 쿠폰 목록
  @GetMapping("/coupons")
  public String cupons(Model model, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    // 특정 회원 찾기
    Long userId = customUserDetails.getUser().getUserId();
    List<CouponDTO> couponList = couponService.getCoupon(userId);
    model.addAttribute("couponList", couponList);

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
      MultipartFile profileImage,
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    try {
      // 수정된 회원 정보 가져오기
      String loginId = customUserDetails.getUsername();
      UpdateProfileDTO result = userService.update(updateProfileDTO, loginId, profileImage);
      model.addAttribute("profile", result);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
    return "redirect:/mypage/account";
  }

  /***
   * 비밀번호 변경 컨트롤러
   * 변경 시 자동으로 로그인 아웃
   * @param newPasswordDTO 비밀번호 변경 DTO
   * @param request 로그인 아웃을 위한 request
   * @return "redirect:/" 성공 시 메인페이지로 이동
   */
  @PostMapping("/account/password")
  public String updatePassword(
      NewPasswordDTO newPasswordDTO,
      HttpServletRequest request,
      Model model,
      @AuthenticationPrincipal CustomUserDetails customUserDetails)
      throws ServletException {
    String loginId = customUserDetails.getUsername();
    boolean result = userService.updatePassword(newPasswordDTO, loginId);
    model.addAttribute("result", result);
    if (result) {
      request.logout();
      return "redirect:/";
    }
    model.addAttribute("profile", userService.profileById(loginId));
    return "user/mypage/account";
  }

  /** 주문 취소 */
  @PostMapping("/order-cancel/{orderId}")
  @ResponseBody
  public Map<String, Object> cancelOrder(
      @PathVariable Long orderId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {

    Map<String, Object> response = new HashMap<>();

    try {
      Long userId = customUserDetails.getUser().getUserId();

      // 주문 조회
      Optional<Order> orderOpt = orderService.findOrderById(orderId);
      if (orderOpt.isPresent()) {
        Order order = orderOpt.get();

        // 주문이 해당 사용자의 것인지 확인
        if (order.getUser() != null && order.getUser().getUserId().equals(userId)) {

          // 주문 상태 확인 (주문완료 또는 배송준비중만 취소 가능)
          if (order.getOrderStatus().equals(Order.OrderStatus.ORDERED)
              || order.getOrderStatus().equals(Order.OrderStatus.PREPARING)) {

            // 주문 상태를 취소로 변경
            orderService.updateOrderStatus(orderId, Order.OrderStatus.CANCELLED);

            // 결제 상태도 환불로 변경
            orderService.updatePaymentStatus(orderId, Order.PaymentStatus.REFUNDED);

            response.put("success", true);
            response.put("message", "주문이 성공적으로 취소되었습니다.");

          } else {
            response.put("success", false);
            response.put("message", "현재 주문 상태에서는 취소할 수 없습니다.");
          }

        } else {
          response.put("success", false);
          response.put("message", "해당 주문에 대한 접근 권한이 없습니다.");
        }

      } else {
        response.put("success", false);
        response.put("message", "주문을 찾을 수 없습니다.");
      }

    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "주문 취소 중 오류가 발생했습니다.");
    }

    return response;
  }
}
