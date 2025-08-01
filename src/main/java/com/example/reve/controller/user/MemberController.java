package com.example.reve.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.reve.dto.CreateUserDTO;
import com.example.reve.service.CouponService;
import com.example.reve.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
@Slf4j
public class MemberController {
  private final UserService userService;
  private final CouponService couponService;

  @GetMapping("/login")
  public String login() {
    return "index";
  }

  @GetMapping("/signup")
  public String signup() {
    return "member/signup";
  }

  @PostMapping("/signup")
  public String signup(CreateUserDTO create) {
    log.info("가입 정보 {}", create);
    Long userId = userService.signup(create);
    couponService.newUserCoupon(userId);
    return "index";
  }
}
