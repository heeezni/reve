package com.example.reve.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.reve.service.PerfumeService;
import com.example.reve.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

  private final PerfumeService perfumeService;
  private final UserService userService;

  @GetMapping("/dashboard")
  public String dashboard(Model model) {
    long totalPerfumeCount = perfumeService.getTotalPerfumeCount();
    long totalUserCount = userService.getTotalUserCount();

    model.addAttribute("totalPerfumeCount", totalPerfumeCount);
    model.addAttribute("totalUserCount", totalUserCount);

    return "admin/dashboard";
  }

  @GetMapping("/product/list")
  public String productList() {
    return "admin/product/list";
  }

  @GetMapping("/product/add")
  public String addProduct() {
    return "admin/product/add";
  }

  @GetMapping("/product/edit")
  public String editProduct() {
    return "admin/product/edit";
  }

  @GetMapping("/order/list")
  public String orderList() {
    return "admin/order/list";
  }

  @GetMapping("/member/list")
  public String memberList() {
    return "admin/member/list";
  }
}
