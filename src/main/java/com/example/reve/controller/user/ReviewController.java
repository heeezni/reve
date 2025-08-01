package com.example.reve.controller.user;

import java.security.Principal;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.example.reve.dto.ReviewCreateDTO;
import com.example.reve.service.ReviewService;

import lombok.RequiredArgsConstructor;

/*
리뷰 url을 매핑해주는 컨트롤러임.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/review")
public class ReviewController {

  private final ReviewService reviewService;

  // 리뷰를 만들어주는 url을 매핑시킴.
  @PostMapping("/create")
  public String createReview(
      @ModelAttribute @Valid ReviewCreateDTO reviewCreateDTO,
      BindingResult bindingResult,
      Principal principal) {
    if (bindingResult.hasErrors()) {
      return "shop/review"; // 폼 다시 보여주기
    }

    reviewService.createReview(reviewCreateDTO, principal);

    return "redirect:/shop/detail?id=" + reviewCreateDTO.getPerfumeId();
  }
}
