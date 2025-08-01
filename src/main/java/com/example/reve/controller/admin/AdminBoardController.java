package com.example.reve.controller.admin;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.reve.dto.QnaResDTO;
import com.example.reve.repository.UserRepository;
import com.example.reve.service.QnaService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/board")
@RequiredArgsConstructor
public class AdminBoardController {

  private final QnaService qnaService;
  private final UserRepository userRepository;

  @GetMapping("/notice/list")
  public String noticeList() {
    return "admin/board/notice/list";
  }

  @GetMapping("/notice/form")
  public String noticeForm() {
    return "admin/board/notice/form";
  }

  @GetMapping("/qna/list")
  public String qnaList(
      Model model,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String category,
      @RequestParam(required = false, defaultValue = "") String filterAndSort) {

    // keyword에 trim() 적용
    if (keyword != null) {
      keyword = keyword.trim();
    }

    String status = null; // QnaService에 전달할 status 값
    Sort sort = Sort.by(Sort.Direction.DESC, "createdAt"); // 기본 정렬: 최신순

    if ("oldest".equals(filterAndSort)) {
      sort = Sort.by(Sort.Direction.ASC, "createdAt");
    } else if ("pending".equals(filterAndSort)) {
      status = "pending";
    } else if ("completed".equals(filterAndSort)) {
      status = "completed";
    }

    Pageable pageableWithSort =
        PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

    // QnaService를 통해 Q&A 목록을 페이징하여 가져오기
    Page<QnaResDTO> qnaPage = qnaService.selectAll(keyword, category, status, pageableWithSort);
    // 가져온 Q&A Page 객체를 "qnas"라는 이름으로 모델에 추가
    model.addAttribute("qnas", qnaPage);
    model.addAttribute("keyword", keyword);
    model.addAttribute("category", category);
    model.addAttribute("filterAndSort", filterAndSort);

    return "admin/board/qna/list";
  }

  @PostMapping("/qna/{qnaId}/answer")
  public String addQnaAnswer(
      @PathVariable Long qnaId,
      @RequestParam("answerContent") String answerContent,
      Principal principal) {
    try {
      if (answerContent == null || answerContent.trim().isEmpty()) {
        throw new IllegalArgumentException("답변 내용은 비워둘 수 없습니다.");
      }
      qnaService.addAnswer(qnaId, answerContent, principal);
      return "redirect:/board/qna/detail/" + qnaId;
    } catch (IllegalAccessException | IllegalArgumentException e) {
      return "redirect:/error"; // 권한 없음 페이지 또는 에러 페이지로 리다이렉트
    }
  }

  @PutMapping("/qna/{qnaId}/answer")
  public String updateQnaAnswer(
      @PathVariable Long qnaId,
      @RequestParam("answerContent") String answerContent,
      Principal principal) {
    try {
      if (answerContent == null || answerContent.trim().isEmpty()) {
        throw new IllegalArgumentException("답변 내용은 비워둘 수 없습니다.");
      }
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
