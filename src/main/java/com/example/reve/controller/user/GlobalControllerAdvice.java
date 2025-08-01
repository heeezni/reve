package com.example.reve.controller.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.ui.Model;
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

  // 결과값
  @ModelAttribute
  public void addFlashSessionAttributesToModel(HttpServletRequest request, Model model) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      Object result = session.getAttribute("result");
      Object result2 = session.getAttribute("result2");

      if (result != null) {
        log.info("result : {}", result);
        model.addAttribute("result", result);
        log.info("model.addAttribute(result) : {}", model.containsAttribute("result"));
        session.removeAttribute("result"); // 한 번만 보여주고 제거
      }

      if (result2 != null) {
        log.info("result2 : {}", result2);
        model.addAttribute("result2", result2);
        session.removeAttribute("result2");
      }
    }
  }
}
