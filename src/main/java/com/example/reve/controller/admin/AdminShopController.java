package com.example.reve.controller.admin;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.reve.dto.PerfumeDetailResponseDto;
import com.example.reve.dto.PerfumeSaveRequestDto;
import com.example.reve.service.PerfumeService;

import lombok.RequiredArgsConstructor;

/*
관리자가 처리하는 shop용 컨트롤러임.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/shop")
public class AdminShopController {

  private final PerfumeService perfumeService;

  // 등록 폼 페이지 보여주기
  @GetMapping("/regist")
  public String showRegisterForm() {
    return "shop/regist"; // 타임리프 템플릿 이름
    // (src/main/resources/templates/admin/perfume_register.html)
  }

  // 향수를 등록하기
  @PostMapping("/regist")
  public String addPerfume(
      PerfumeSaveRequestDto requestDto,
      @RequestParam("imageFile") MultipartFile imageFile,
      @RequestParam("hoverImageFile") MultipartFile hoverImageFile) {

    String imageUrl = perfumeService.storeImage(imageFile);
    String hoverImageUrl = perfumeService.storeImage(hoverImageFile);

    requestDto.setImageUrl(imageUrl);
    requestDto.setHoverImageUrl(hoverImageUrl);

    perfumeService.savePerfume(requestDto);

    return "redirect:/admin/product/list";
  }

  // 향수를 삭제하기 (개별 및 선택 삭제 가능)
  @PostMapping("/delete")
  public String deletePerfumes(
      @RequestParam(value = "perfumeIdList", required = false) List<Long> perfumeIdList,
      @RequestParam(value = "id", required = false) Long perfumeId) {
    if (perfumeIdList != null && !perfumeIdList.isEmpty()) {
      perfumeService.deletePerfumes(perfumeIdList);
    } else if (perfumeId != null) {
      perfumeService.deletePerfume(perfumeId);
    }
    return "redirect:/admin/product/list";
  }

  // 향수 수정폼 가져오기
  @GetMapping("/edit/{perfumeId}")
  public String showEditForm(@PathVariable("perfumeId") Long perfumeId, Model model) {
    PerfumeDetailResponseDto perfume = perfumeService.getPerfumeDetail(perfumeId);
    model.addAttribute("perfume", perfume);
    return "shop/edit";
  }

  // 향수 수정하기
  @PostMapping("/edit/{perfumeId}")
  public String updatePerfume(
      @PathVariable("perfumeId") Long perfumeId,
      @ModelAttribute PerfumeSaveRequestDto requestDto,
      @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
      @RequestParam(value = "hoverImageFile", required = false) MultipartFile hoverImageFile) {

    perfumeService.updatePerfume(perfumeId, requestDto, imageFile, hoverImageFile);

    return "redirect:/admin/product/list";
  }
}
