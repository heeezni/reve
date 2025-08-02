package com.example.reve.controller.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.reve.domain.CustomUserDetails;
import com.example.reve.domain.User;
import com.example.reve.dto.CreateUserDTO;
import com.example.reve.dto.LoginUserDTO;
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
    return "member/login";
  }

  @PostMapping("/login")
  public String login(LoginUserDTO loginUserDTO, HttpServletRequest request) {
    log.info("로그인 시작 : {}", loginUserDTO);
    User loginUser = userService.login(loginUserDTO);
    if (loginUser != null) {
      CustomUserDetails customUserDetails = new CustomUserDetails(loginUser);
      UsernamePasswordAuthenticationToken token =
          new UsernamePasswordAuthenticationToken(
              customUserDetails, null, customUserDetails.getAuthorities());
      SecurityContextHolder.getContext().setAuthentication(token);
      HttpSession session = request.getSession();
      session.setAttribute(
          HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
          SecurityContextHolder.getContext());
      log.info("로그인 성공확인 : {}", loginUser);
      return "redirect:/";
    } else {
      return "redirect:/login?error";
    }
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
