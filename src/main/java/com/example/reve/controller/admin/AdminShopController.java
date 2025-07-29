package com.example.reve.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.reve.dto.PerfumeSaveRequestDto;
import com.example.reve.service.PerfumeService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/shop")
public class AdminShopController {

  private final PerfumeService perfumeService;

  @PostMapping("/add")
  @ResponseBody
  public ResponseEntity<String> addPerfume(@RequestBody PerfumeSaveRequestDto requestDto) {
    Long perfumeId = perfumeService.savePerfume(requestDto);
    return ResponseEntity.ok("향수 등록 완료 (ID : ");
  }
}
