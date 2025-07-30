package com.example.reve.controller.user;

import java.util.List;

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
  public String qnaList(Model model) { // Model 객체 추가
    // 1. QnaService를 통해 모든 Q&A 목록가져오기
    List<QnaResDto> qnas = qnaService.selectAll();
    // 2. 가져온 Q&A 목록을 "qnas"라는 이름으로 모델에 추가
    model.addAttribute("qnas", qnas);
    // 3. "board/qna/list.html" 뷰 반환
    return "board/qna/list";
  }

  @GetMapping("/qna/detail")
  public String qnaDetail() {
    return "board/qna/detail";
  }

  @GetMapping("/qna/form")
  public String qnaForm() {
    return "board/qna/form";
  }
}
