package com.example.reve.controller.admin;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.NoSuchElementException;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.reve.dto.NoticeReqDTO;
import com.example.reve.dto.NoticeResDTO;
import com.example.reve.dto.QnaResDTO;
import com.example.reve.service.NoticeService;
import com.example.reve.service.QnaService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/board")
@RequiredArgsConstructor
public class AdminBoardController {

  private final QnaService qnaService;
  private final NoticeService noticeService;

  @GetMapping("/notice/register")
  public String noticeRegisterForm(Model model) {
    model.addAttribute("noticeReqDTO", new NoticeReqDTO());
    return "admin/board/notice/register";
  }

  @GetMapping("/notice/edit/{noticeId}")
  public String noticeEditForm(@PathVariable Long noticeId, Model model, Principal principal) {
    try {
      NoticeResDTO notice = noticeService.getNoticeById(noticeId);
      model.addAttribute("notice", notice);
      model.addAttribute("isAdmin", isAdmin(principal)); // isAdmin 추가
      return "admin/board/notice/edit";
    } catch (NoSuchElementException e) {
      model.addAttribute("errorMessage", e.getMessage());
      return "common/error";
    }
  }

  @PostMapping("/notice/register")
  public String registerNotice(
      @Valid @ModelAttribute NoticeReqDTO noticeReqDTO,
      BindingResult bindingResult,
      Principal principal,
      RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      return "admin/board/notice/register";
    }
    try {
      noticeService.createNotice(noticeReqDTO, principal);
      redirectAttributes.addFlashAttribute("successMessage", "공지사항이 성공적으로 등록되었습니다.");
      return "redirect:/board/notice/list";
    } catch (AccessDeniedException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
      return "redirect:/admin/board/notice/register";
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("errorMessage", "공지사항 등록에 실패했습니다.");
      return "redirect:/admin/board/notice/register";
    }
  }

  @PutMapping("/notice/edit/{noticeId}")
  @ResponseBody
  public ResponseEntity<?> updateNotice(
      @PathVariable Long noticeId,
      @Valid @ModelAttribute NoticeReqDTO noticeReqDTO,
      BindingResult bindingResult,
      Principal principal) {
    if (bindingResult.hasErrors()) {
      return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
    }
    try {
      NoticeResDTO updatedNotice = noticeService.updateNotice(noticeId, noticeReqDTO, principal);
      return ResponseEntity.ok(Map.of("noticeId", updatedNotice.getNoticeId()));
    } catch (IOException | AccessDeniedException e) {
      return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(500).body(Map.of("error", "공지사항 수정에 실패했습니다."));
    }
  }

  @GetMapping("/qna/list")
  public String qnaList(
      Model model,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String filterAndSort,
      Principal principal) {

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
    Page<QnaResDTO> qnaPage =
        qnaService.selectAll(keyword, category, status, pageableWithSort, principal);
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
    } catch (IllegalArgumentException e) {
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
    } catch (IllegalArgumentException e) {
      return "redirect:/error";
    }
  }

  @DeleteMapping("/qna/{qnaId}/answer")
  public String deleteQnaAnswer(@PathVariable Long qnaId, Principal principal) {
    try {
      qnaService.deleteAnswer(qnaId, principal);
      return "redirect:/board/qna/detail/" + qnaId;
    } catch (IllegalArgumentException e) {
      return "redirect:/error";
    }
  }

  @DeleteMapping("/notice/{noticeId}")
  public ResponseEntity<String> deleteNotice(@PathVariable Long noticeId, Principal principal) {
    try {
      noticeService.deleteNotice(noticeId, principal);
      return ResponseEntity.ok("공지사항이 성공적으로 삭제되었습니다.");
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(403).body("권한이 없습니다.");
    } catch (NoSuchElementException e) {
      return ResponseEntity.status(404).body("공지사항을 찾을 수 없습니다.");
    } catch (Exception e) {
      return ResponseEntity.status(500).body("공지사항 삭제 중 오류가 발생했습니다: " + e.getMessage());
    }
  }

  private boolean isAdmin(Principal principal) {
    if (principal == null) {
      return false;
    }
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null
        && authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
  }
}
