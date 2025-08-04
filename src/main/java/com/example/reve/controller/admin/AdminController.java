package com.example.reve.controller.admin;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.reve.domain.CustomUserDetails;
import com.example.reve.domain.Order;
import com.example.reve.domain.Role;
import com.example.reve.domain.User;
import com.example.reve.repository.UserRepository;
import com.example.reve.service.OrderService;
import com.example.reve.service.PerfumeService;
import com.example.reve.service.UserService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

  private final PerfumeService perfumeService;
  private final UserService userService;
  private final UserRepository userRepository;
  private final OrderService orderService;

  @GetMapping("/dashboard")
  public String dashboard() {
    return "redirect:/admin/product/list";
  }

  @GetMapping("/product/list")
  public String productList(Model model) {
    model.addAttribute("perfumes", perfumeService.getAllPerfumes());
    return "admin/product/list";
  }

  @GetMapping("/order/list")
  public String orderList(
      @RequestParam(value = "orderNumber", required = false) String orderNumber,
      @RequestParam(value = "customerName", required = false) String customerName,
      @RequestParam(value = "orderStatus", required = false) String orderStatus,
      @RequestParam(value = "paymentStatus", required = false) String paymentStatus,
      Model model) {

    // 주문 목록 조회
    List<Order> orders = orderService.getAllOrders();

    // 필터링 적용
    if (orderNumber != null && !orderNumber.trim().isEmpty()) {
      orders =
          orders.stream().filter(order -> order.getOrderNumber().contains(orderNumber)).toList();
    }

    if (customerName != null && !customerName.trim().isEmpty()) {
      orders =
          orders.stream()
              .filter(
                  order ->
                      order.getUser() != null
                          && order.getUser().getName() != null
                          && order.getUser().getName().contains(customerName))
              .toList();
    }

    if (orderStatus != null && !orderStatus.trim().isEmpty()) {
      orders = orders.stream().filter(order -> order.getOrderStatus().equals(orderStatus)).toList();
    }

    if (paymentStatus != null && !paymentStatus.trim().isEmpty()) {
      orders =
          orders.stream().filter(order -> order.getPaymentStatus().equals(paymentStatus)).toList();
    }

    model.addAttribute("orders", orders);

    return "admin/order/list";
  }

  @PostMapping("/order/update-status")
  @ResponseBody
  public String updateOrderStatus(@RequestBody OrderStatusUpdateRequest request) {
    try {
      orderService.updateOrderStatus(request.getOrderId(), request.getOrderStatus());
      return "{\"success\": true, \"message\": \"주문 상태가 변경되었습니다.\"}";
    } catch (Exception e) {
      return "{\"success\": false, \"message\": \"" + e.getMessage() + "\"}";
    }
  }

  // 내부 클래스로 DTO 정의
  @Setter
  @Getter
  public static class OrderStatusUpdateRequest {
    private Long orderId;
    private String orderStatus;
  }

  @GetMapping("/member/list")
  public String memberList(Model model) {
    model.addAttribute("users", userService.getAllUsers());
    return "admin/member/list";
  }

  @GetMapping("/member/delete")
  public String deleteMember(@RequestParam("userId") Long userId) {
    userService.deleteUser(userId);
    return "redirect:/admin/member/list";
  }

  @PostMapping("/member/updateRole")
  public String updateMemberRole(
      @RequestParam("userId") Long userId,
      @RequestParam("role") String role,
      RedirectAttributes redirectAttributes) {
    userService.updateUserRole(userId, Role.valueOf(role));
    redirectAttributes.addFlashAttribute("message", "권한이 변경되었습니다.");
    return "redirect:/admin/member/list";
  }

  @GetMapping("/member/mypage/{userId}")
  public String viewUserMypage(
      @PathVariable("userId") Long userId,
      Model model,
      @AuthenticationPrincipal CustomUserDetails currentUser) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
    model.addAttribute("mypage", userService.selectMypage(user.getLoginId()));
    model.addAttribute("currentUserId", currentUser.getUser().getUserId());
    return "user/mypage/index";
  }
}
