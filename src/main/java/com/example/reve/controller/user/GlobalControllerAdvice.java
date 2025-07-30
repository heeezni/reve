package com.example.reve.controller.user;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import lombok.extern.log4j.Log4j2;

@ControllerAdvice
@Log4j2
public class GlobalControllerAdvice {
  @ModelAttribute("loginUser")
  public Object addLoginUserModel(HttpSession session) {
    if (session.getAttribute("loginUser") != null) {
      log.info("세션에서 유저 정보 가져오기 성공 : {}", session.getAttribute("loginUser"));
    }
    return session.getAttribute("loginUser");
  }
}
