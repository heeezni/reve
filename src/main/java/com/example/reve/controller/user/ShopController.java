package com.example.reve.controller.user;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.reve.domain.Perfume;
import com.example.reve.domain.Review;
import com.example.reve.domain.User;
import com.example.reve.dto.PerfumeDetailResponseDto;
import com.example.reve.dto.PerfumeListResponseDto;
import com.example.reve.repository.UserRepository;
import com.example.reve.service.PerfumeService;
import com.example.reve.service.ReviewService;
import com.example.reve.service.WishListService;

import lombok.RequiredArgsConstructor;

/*
Shop에 관련된 url을 매핑하기 위한 컨트롤러
 */
@Controller
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

  private final PerfumeService perfumeService;
  private final UserRepository userRepository;
  private final WishListService wishListService;
  private final ReviewService reviewService;

  @GetMapping("/list")
  public String productList(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String scent,
      @RequestParam(required = false) String search,
      Model model) {

    Page<Perfume> perfumePage = perfumeService.getPerfumes(search, scent, sort, page, size);
    Page<PerfumeListResponseDto> dtoPage = perfumePage.map(PerfumeListResponseDto::fromEntity);

    model.addAttribute("perfumePage", dtoPage);
    model.addAttribute("search", search);
    model.addAttribute("scent", scent);
    model.addAttribute("sort", sort);

    return "shop/list";
  }

  @GetMapping("/detail")
  public String productDetail(
      @RequestParam("id") Long perfumeId, Model model, Principal principal) {
    PerfumeDetailResponseDto perfumeDetail = perfumeService.getPerfumeDetail(perfumeId);
    model.addAttribute("perfume", perfumeDetail);

    // 관련 상품 (같은 향 기준 최대 4개)
    String scent = perfumeDetail.getScent();
    List<PerfumeListResponseDto> relatedPerfumes = perfumeService.getRelatedPerfumesByScent(scent);

    // 현재 상세 상품은 관련 상품에서 제외
    relatedPerfumes =
        relatedPerfumes.stream().filter(p -> !p.getPerfumeId().equals(perfumeId)).toList();

    model.addAttribute("relatedPerfumes", relatedPerfumes);

    // 로그인한 유저가 있을 경우 찜 여부 추가
    boolean isWished = false;
    if (principal != null) {
      String loginId = principal.getName();
      User user = userRepository.findByLoginId(loginId).orElse(null);
      if (user != null) {
        isWished = wishListService.isWished(user, perfumeId);
      }
    }
    model.addAttribute("isWished", isWished);
    model.addAttribute("isLoggedIn", principal != null);

    // 평점별 리뷰 개수 데이터 추가
    Map<Integer, Long> reviewCountsByRating = reviewService.getReviewCountsByRating(perfumeId);
    model.addAttribute("reviewCountsByRating", reviewCountsByRating);

    // 총 리뷰 개수 계산 및 모델 추가
    long totalReviews = reviewCountsByRating.values().stream().mapToLong(Long::longValue).sum();
    model.addAttribute("totalReviews", totalReviews);

    // ratingPercentMap 생성 (totalReviews 변수 이용)
    Map<Integer, String> ratingPercentMap = new HashMap<>();
    for (Integer star : reviewCountsByRating.keySet()) {
      Long count = reviewCountsByRating.get(star);
      double percent = totalReviews > 0 ? (count * 100.0 / totalReviews) : 0.0;
      ratingPercentMap.put(star, String.format("%.1f%%", percent));
    }
    model.addAttribute("ratingPercentMap", ratingPercentMap);

    // 리뷰 리스트 불러와서 모델에 추가
    List<Review> reviews = reviewService.getReviewsWithUserByPerfumeId(perfumeId);
    model.addAttribute("reviews", reviews);

    return "shop/detail";
  }
}
