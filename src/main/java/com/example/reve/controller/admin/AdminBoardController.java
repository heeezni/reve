package com.example.reve.controller.admin;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.example.reve.service.QnaService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/board")
@RequiredArgsConstructor
public class AdminBoardController {

  private final QnaService qnaService;

  @GetMapping("/notice/list")
  public String noticeList() {
    return "admin/board/notice/list";
  }

  @GetMapping("/notice/form")
  public String noticeForm() {
    return "admin/board/notice/form";
  }

  @GetMapping("/qna/list")
  public String qnaList() {
    return "admin/board/qna/list";
  }

  @GetMapping("/qna/reply")
  public String qnaReply() {
    return "admin/board/qna/reply";
  }

  @PostMapping("/qna/{qnaId}/answer")
  public String addQnaAnswer(
      @PathVariable Long qnaId,
      @RequestParam("answerContent") String answerContent,
      Principal principal) {
    try {
      qnaService.addAnswer(qnaId, answerContent, principal);
      return "redirect:/board/qna/detail/" + qnaId;
    } catch (IllegalAccessException e) {
      return "redirect:/error"; // 권한 없음 페이지 또는 에러 페이지로 리다이렉트
    } catch (IllegalArgumentException e) {
      return "redirect:/error"; // Q&A를 찾을 수 없는 경우 등
    }
  }

  @PutMapping("/qna/{qnaId}/answer")
  public String updateQnaAnswer(
      @PathVariable Long qnaId,
      @RequestParam("answerContent") String answerContent,
      Principal principal) {
    try {
      qnaService.updateAnswer(qnaId, answerContent, principal);
      return "redirect:/board/qna/detail/" + qnaId;
    } catch (IllegalAccessException | IllegalArgumentException e) {
      return "redirect:/error";
    }
  }

  @DeleteMapping("/qna/{qnaId}/answer")
  public String deleteQnaAnswer(@PathVariable Long qnaId, Principal principal) {
    try {
      qnaService.deleteAnswer(qnaId, principal);
      return "redirect:/board/qna/detail/" + qnaId;
    } catch (IllegalAccessException | IllegalArgumentException e) {
      return "redirect:/error";
    }
  }
}
