package com.example.reve.controller.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.reve.dto.QnaReqDto;
import com.example.reve.dto.QnaResDto;
import com.example.reve.service.QnaService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

  private final QnaService qnaService; // 서비스 주입 (QnaService)

  // Q&A 생성 API
  @PostMapping("/qna")
  @ResponseBody // View가 아닌 데이터(JSON)를 반환
  public ResponseEntity<QnaResDto> createQna(@ModelAttribute QnaReqDto reqDto) {
    QnaResDto qna = qnaService.createQna(reqDto);
    return ResponseEntity.ok(qna); // 성공(200 OK) 응답과 함께 생성된 Q&A 정보 반환
  }

  @GetMapping("/notice/list")
  public String noticeList() {
    return "board/notice/list";
  }

  @GetMapping("/notice/detail")
  public String noticeDetail() {
    return "board/notice/detail";
  }

  @GetMapping("/qna/list")
  public String qnaList(
      Model model,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    // 1. QnaService를 통해 Q&A 목록을 페이징하여 가져오기
    Page<QnaResDto> qnaPage = qnaService.selectAll(pageable);
    // 2. 가져온 Q&A Page 객체를 "qnas"라는 이름으로 모델에 추가 (Thymeleaf에서 qnas로 사용)
    model.addAttribute("qnas", qnaPage);
    // 3. "board/qna/list.html" 뷰 반환
    return "board/qna/list";
  }

  @GetMapping("/qna/detail/{qnaId}")
  public String qnaDetail(@PathVariable Long qnaId, Model model) {
    QnaResDto qna = qnaService.getQnaById(qnaId);
    model.addAttribute("qna", qna);
    return "board/qna/detail";
  }

  @GetMapping("/qna/form")
  public String qnaForm() {
    return "board/qna/form";
  }
}
