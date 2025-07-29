package com.example.reve.controller.user;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.reve.dto.PerfumeDetailResponseDto;
import com.example.reve.dto.PerfumeListResponseDto;
import com.example.reve.service.PerfumeService;

import lombok.RequiredArgsConstructor;

/*
Shop에 관련된 url을 매핑하기 위한 컨트롤러
 */
@Controller
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

  private final PerfumeService perfumeService;

  // 리스트를 조회하기 위한 GET 매핑임(필터링 가능).
  @GetMapping("/list")
  public String productList(
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String scent,
      @RequestParam(required = false) String search,
      Model model) {
    // 초기에는 모든 향수 목록을 보여줌.
    List<PerfumeListResponseDto> perfumeList = perfumeService.getAllPerfumes();

    if (search != null && !search.isEmpty()) {
      // 검색어가 있으면 검색어 기준으로 필터링 (향, 정렬 무시하거나 같이 적용 가능)
      perfumeList =
          perfumeService.searchByName(search).stream()
              .map(PerfumeListResponseDto::fromEntity)
              .toList();
    } else if (scent != null && !scent.isEmpty()) { // 향이 선택된다면
      // 정렬기준이 선택되지 않았다면
      if (sort == null || sort.isEmpty()) {
        // 향으로 필터링
        perfumeList =
            perfumeService.findAllByScent(scent).stream()
                .map(PerfumeListResponseDto::fromEntity)
                .toList();
      } else {
        // 향 + 정렬로 필터링
        perfumeList =
            perfumeService.findByScentAndSort(scent, sort).stream()
                .map(PerfumeListResponseDto::fromEntity)
                .toList();
      }
    } else { // 향이 선택되지 않았다면
      // 정렬기준이 선택되지 않았다면
      if (sort == null || sort.isEmpty()) {
        // 디폴트는 최신순 정렬임.
        perfumeList =
            perfumeService.findAllByLatest().stream()
                .map(PerfumeListResponseDto::fromEntity)
                .toList();
      } else { // 정렬기준이 선택되었다면
        switch (sort) {
          // 가격 오름차순
          case "price_asc":
            perfumeList =
                perfumeService.findAllByPriceAsc().stream()
                    .map(PerfumeListResponseDto::fromEntity)
                    .toList();
            break;
          // 가격 내림차순
          case "price_desc":
            perfumeList =
                perfumeService.findAllByPriceDesc().stream()
                    .map(PerfumeListResponseDto::fromEntity)
                    .toList();
            break;
          // 리뷰순
          case "review":
            perfumeList =
                perfumeService.findAllByReviewCount().stream()
                    .map(PerfumeListResponseDto::fromEntity)
                    .toList();
            break;
          // 디폴트는 최신순
          default:
            perfumeList =
                perfumeService.findAllByLatest().stream()
                    .map(PerfumeListResponseDto::fromEntity)
                    .toList();
            break;
        }
      }
    }

    model.addAttribute("search", search);
    model.addAttribute("scent", scent);
    model.addAttribute("perfumeList", perfumeList);
    model.addAttribute("sort", sort);
    return "shop/list";
  }

  @GetMapping("/detail")
  public String productDetail(@RequestParam("id") Long perfumeId, Model model) {
    PerfumeDetailResponseDto perfumeDetail = perfumeService.getPerfumeDetail(perfumeId);
    model.addAttribute("perfume", perfumeDetail);

    // 관련 상품 (같은 향 기준 최대 4개)
    String scent = perfumeDetail.getScent();
    List<PerfumeListResponseDto> relatedPerfumes = perfumeService.getRelatedPerfumesByScent(scent);

    // 현재 상세 상품은 관련 상품에서 제외
    relatedPerfumes =
        relatedPerfumes.stream().filter(p -> !p.getPerfumeId().equals(perfumeId)).toList();

    model.addAttribute("relatedPerfumes", relatedPerfumes);

    return "shop/detail";
  }
}
