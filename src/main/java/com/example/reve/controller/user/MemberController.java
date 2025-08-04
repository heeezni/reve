package com.example.reve.controller.user;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.reve.dto.CreateUserDTO;
import com.example.reve.service.CouponService;
import com.example.reve.service.PointService;
import com.example.reve.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
  private final UserService userService;
  private final CouponService couponService;
  private final PointService pointService;

  @GetMapping("/login")
  public String login(@RequestParam(value = "error", required = false) String error, Model model) {
    if ("true".equals(error)) {
      model.addAttribute("loginError", true); // 모델에 플래그 전달
    }
    return "member/login";
  }

  @GetMapping("/signup")
  public String signup() {
    return "member/signup";
  }

  @PostMapping("/check/loginId")
  @ResponseBody
  public ResponseEntity<String> checkLoginId(@RequestParam String loginId) {
    boolean result = userService.checklogin(loginId);
    if (result) {
      return ResponseEntity.ok("duplicate");
    } else {
      return ResponseEntity.ok("available");
    }
  }

  @PostMapping("/signup")
  public String signup(CreateUserDTO create) {
    Long userId = userService.signup(create);
    couponService.newUserCoupon(userId);
    pointService.getPoint(userId, 1000);
    return "index";
  }

  @PostMapping("/delete")
  public String deleteMember(Principal principal) {
    String loginId = principal.getName();
    userService.delete(loginId);
    return "index";
  }
}
