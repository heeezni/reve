package com.example.reve.controller.user;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.reve.domain.Perfume;
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
