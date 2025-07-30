package com.example.reve.controller.user;

import java.io.IOException;
import java.security.Principal;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.reve.dto.QnaReqDTO;
import com.example.reve.dto.QnaResDTO;
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
  public ResponseEntity<?> createQna(
      @Valid @ModelAttribute QnaReqDTO reqDto, BindingResult bindingResult) {
    // QnaReqDTO의 유효성 검사 수행
    if (bindingResult.hasErrors()) {
      StringBuilder errorMessage = new StringBuilder();
      // 모든 유효성 검사 오류 메시지를 취합하여 반환
      for (FieldError error : bindingResult.getFieldErrors()) {
        errorMessage.append(error.getDefaultMessage()).append("\n");
      }
      return ResponseEntity.badRequest().body(errorMessage.toString());
    }
    try {
      QnaResDTO qna = qnaService.createQna(reqDto);
      return ResponseEntity.ok(qna); // 성공(200 OK) 응답과 함께 생성된 Q&A 정보 반환
    } catch (IOException e) {
      // 파일 업로드 관련 예외 처리
      return ResponseEntity.badRequest().body("파일 업로드 실패: " + e.getMessage());
    } catch (IllegalArgumentException e) {
      // 서비스 로직에서 발생하는 유효성 검사 예외 처리 (예: 비밀글 비밀번호 누락)
      return ResponseEntity.badRequest().body("문의 등록 실패: " + e.getMessage());
    } catch (Exception e) {
      // 그 외 예상치 못한 예외 처리
      return ResponseEntity.internalServerError().body("문의 등록 중 알 수 없는 오류가 발생했습니다.");
    }
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
          Pageable pageable,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String category,
      @RequestParam(required = false, defaultValue = "") String filterAndSort) {

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

    // 1. QnaService를 통해 Q&A 목록을 페이징하여 가져오기
    Page<QnaResDTO> qnaPage = qnaService.selectAll(keyword, category, status, pageableWithSort);
    // 2. 가져온 Q&A Page 객체를 "qnas"라는 이름으로 모델에 추가 (Thymeleaf에서 qnas로 사용)
    model.addAttribute("qnas", qnaPage);
    // 3. "board/qna/list.html" 뷰 반환
    return "board/qna/list";
  }

  @GetMapping("/qna/detail/{qnaId}")
  public String qnaDetail(@PathVariable Long qnaId, Model model, Principal principal) {
    try {
      // 비밀번호 없이 조회를 시도. 공개글이거나 관리자면 성공.
      QnaResDTO qna = qnaService.getQnaById(qnaId, principal, null);
      model.addAttribute("qna", qna);
      return "board/qna/detail";
    } catch (IllegalAccessException e) {
      // 비밀글이고 권한이 없으면 비밀번호 입력 페이지로 이동
      return "redirect:/board/qna/password/" + qnaId;
    } catch (IllegalArgumentException e) {
      model.addAttribute("errorMessage", e.getMessage());
      return "common/error";
    }
  }

  @GetMapping("/qna/form")
  public String qnaForm() {
    return "board/qna/form";
  }

  // 비밀글 비밀번호 입력 폼을 보여주는 엔드포인트
  @GetMapping("/qna/password/{qnaId}")
  public String showPasswordForm(@PathVariable Long qnaId, Model model) {
    model.addAttribute("qnaId", qnaId);
    return "board/qna/password_check";
  }

  // 비밀글 비밀번호를 검증하고 상세 페이지를 보여주는 엔드포인트
  @PostMapping("/qna/password/{qnaId}")
  public String verifyPassword(
      @PathVariable Long qnaId,
      @RequestParam String password,
      Model model,
      Principal principal,
      RedirectAttributes redirectAttributes) {
    try {
      // 비밀번호와 함께 조회를 시도
      QnaResDTO qna = qnaService.getQnaById(qnaId, principal, password);
      model.addAttribute("qna", qna);
      return "board/qna/detail"; // 성공 시 상세 페이지 보여주기
    } catch (IllegalAccessException e) {
      // 비밀번호가 틀렸을 경우, 에러 메시지와 함께 비밀번호 입력 페이지로 리다이렉트
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
      return "redirect:/board/qna/password/" + qnaId;
    } catch (IllegalArgumentException e) {
      // 존재하지 않는 Q&A 등 다른 에러 처리
      model.addAttribute("errorMessage", e.getMessage());
      return "common/error";
    }
  }
}
