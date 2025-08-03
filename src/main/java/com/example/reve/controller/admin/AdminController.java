package com.example.reve.controller.admin;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.reve.domain.CustomUserDetails;
import com.example.reve.domain.Role;
import com.example.reve.domain.User;
import com.example.reve.repository.UserRepository;
import com.example.reve.service.PerfumeService;
import com.example.reve.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

  private final PerfumeService perfumeService;
  private final UserService userService;
  private final UserRepository userRepository;

  @GetMapping("/dashboard")
  public String dashboard(Model model) {
    long totalPerfumeCount = perfumeService.getTotalPerfumeCount();
    long totalUserCount = userService.getTotalUserCount();

    model.addAttribute("totalPerfumeCount", totalPerfumeCount);
    model.addAttribute("totalUserCount", totalUserCount);

    return "admin/dashboard";
  }

  @GetMapping("/product/list")
  public String productList(Model model) {
    model.addAttribute("perfumes", perfumeService.getAllPerfumes());
    return "admin/product/list";
  }

  @GetMapping("/order/list")
  public String orderList() {
    return "admin/order/list";
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
      @RequestParam("userId") Long userId, @RequestParam("role") String role) {
    userService.updateUserRole(userId, Role.valueOf(role));
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
