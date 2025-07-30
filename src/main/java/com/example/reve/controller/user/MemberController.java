package com.example.reve.controller.user;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.reve.domain.User;
import com.example.reve.dto.CreateUserDTO;
import com.example.reve.dto.LoginUserDTO;
import com.example.reve.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
@Slf4j
public class MemberController {
  private final UserService userService;

  @GetMapping("/login")
  public String login() {
    return "member/login";
  }

  @PostMapping("/login")
  public String login(LoginUserDTO loginUserDTO, HttpSession session) {
    log.info("로그인 시작 : {}", loginUserDTO);
    User loginUser = userService.login(loginUserDTO);
    session.setAttribute("loginUser", loginUser);
    log.info("세션에 보관 중인 유저 : {}", loginUser);
    return "redirect:/";
  }

  @GetMapping("/signup")
  public String signup() {
    return "member/signup";
  }

  @PostMapping("/signup")
  public String signup(CreateUserDTO create) {
    log.info("가입 정보 {}", create);
    userService.signup(create);
    return "index";
  }
}
