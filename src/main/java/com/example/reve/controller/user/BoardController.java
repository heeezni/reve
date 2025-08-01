package com.example.reve.controller.user;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.reve.domain.Role;
import com.example.reve.domain.User;
import com.example.reve.dto.QnaReqDTO;
import com.example.reve.dto.QnaResDTO;
import com.example.reve.repository.UserRepository;
import com.example.reve.service.NoticeService;
import com.example.reve.service.PerfumeService;
import com.example.reve.service.QnaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

  private final QnaService qnaService;
  private final UserRepository userRepository;
  private final PerfumeService perfumeService;
  private final NoticeService noticeService;

  // Q&A 생성 API
  @PostMapping("/qna")
  @ResponseBody // View가 아닌 데이터(JSON)를 반환
  public ResponseEntity<?> createQna(
      @Valid @ModelAttribute QnaReqDTO reqDto, BindingResult bindingResult, Principal principal) {
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
      QnaResDTO qna = qnaService.createQna(reqDto, principal);
      return ResponseEntity.ok(qna); // 성공(200 OK) 응답과 함께 생성된 Q&A 정보 반환
    } catch (IOException e) {
      // 파일 업로드 관련 예외 처리
      return ResponseEntity.badRequest().body("파일 업로드 실패: " + e.getMessage());
    } catch (IllegalArgumentException e) {
      // 서비스 로직에서 발생하는 유효성 검사 예외 처리
      return ResponseEntity.badRequest().body("문의 등록 실패: " + e.getMessage());
    } catch (Exception e) {
      // 그 외 예상치 못한 예외 처리
      return ResponseEntity.internalServerError().body("문의 등록 중 알 수 없는 오류가 발생했습니다.");
    }
  }

  @GetMapping("/notice/list")
  public String noticeList(
      Model model,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String category,
      @RequestParam(required = false, defaultValue = "createdAt,desc") String sort, // sort 파라미터 추가
      Principal principal) {
    model.addAttribute("isAdmin", isAdmin(principal));

    // sort 파라미터 파싱
    Sort sortObj;
    if (sort.equals("createdAt,desc")) {
      sortObj =
          Sort.by(Sort.Direction.DESC, "important").and(Sort.by(Sort.Direction.DESC, "createdAt"));
    } else if (sort.equals("hit,desc")) {
      sortObj = Sort.by(Sort.Direction.DESC, "important").and(Sort.by(Sort.Direction.DESC, "hit"));
    } else if (sort.equals("title,asc")) {
      sortObj = Sort.by(Sort.Direction.DESC, "important").and(Sort.by(Sort.Direction.ASC, "title"));
    } else {
      sortObj =
          Sort.by(Sort.Direction.DESC, "important")
              .and(Sort.by(Sort.Direction.DESC, "createdAt")); // 기본 정렬
    }

    Pageable sortedPageable =
        PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortObj);

    // 공지사항 목록을 가져오는 로직 추가
    Page<com.example.reve.domain.Notice> noticePage =
        noticeService.getAllNotices(sortedPageable, keyword, category); // sortedPageable 전달
    model.addAttribute("notices", noticePage);
    model.addAttribute("keyword", keyword);
    model.addAttribute("category", category);
    model.addAttribute("sort", sort); // sort 값도 모델에 추가

    return "board/notice/list";
  }

  @GetMapping("/notice/detail/{noticeId}")
  public String noticeDetail(@PathVariable Long noticeId, Model model, Principal principal) {
    model.addAttribute("isAdmin", isAdmin(principal));

    try {
      // 공지사항 상세 내용을 가져오는 로직 추가
      com.example.reve.domain.Notice notice = noticeService.getNoticeById(noticeId);
      model.addAttribute("notice", notice);

      // 이전/다음 공지사항 가져오기
      noticeService
          .getPrevNotice(noticeId)
          .ifPresent(prevNotice -> model.addAttribute("prevNotice", prevNotice));
      noticeService
          .getNextNotice(noticeId)
          .ifPresent(nextNotice -> model.addAttribute("nextNotice", nextNotice));

      // 관련 공지사항 가져오기 (현재 공지사항 제외, 같은 카테고리 내에서, 최신순 5개)
      List<com.example.reve.domain.Notice> relatedNotices =
          noticeService.getRelatedNotices(noticeId, notice.getCategory(), 5);
      model.addAttribute("relatedNotices", relatedNotices);

      return "board/notice/detail";
    } catch (NoSuchElementException e) {
      model.addAttribute("errorMessage", e.getMessage());
      return "common/error"; // 또는 공지사항 목록 페이지로 리다이렉트
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

    // 1. QnaService를 통해 Q&A 목록을 페이징하여 가져오기
    Page<QnaResDTO> qnaPage = qnaService.selectAll(keyword, category, status, pageableWithSort);
    // 2. 가져온 Q&A Page 객체를 "qnas"라는 이름으로 모델에 추가 (Thymeleaf에서 qnas로 사용)
    model.addAttribute("qnas", qnaPage);
    // 3. "board/qna/list.html" 뷰 반환
    return "board/qna/list";
  }

  @GetMapping("/qna/detail/{qnaId}")
  public String qnaDetail(@PathVariable Long qnaId, Model model, Principal principal) {
    log.info("qnaDetail: Accessing Q&A with ID: {}", qnaId); // Q&A ID 로깅
    try {
      // 비밀번호 없이 조회를 시도. 공개글이거나 관리자면 성공.
      QnaResDTO qna = qnaService.getQnaById(qnaId, principal);
      model.addAttribute("qna", qna);

      // 현재 로그인한 사용자 정보 확인
      boolean isAuthor = false;
      boolean isAdmin = false;

      log.info("qnaDetail: Principal is null: {}", (principal == null)); // Principal null 여부
      if (principal != null) {
        String loggedInUsername = principal.getName();
        log.info("qnaDetail: Logged-in username: {}", loggedInUsername); // 로그인한 사용자 이름
        User loggedInUser = userRepository.findByLoginId(loggedInUsername).orElse(null);
        log.info(
            "qnaDetail: Found loggedInUser: {}",
            (loggedInUser != null ? loggedInUser.getLoginId() : "null")); // 조회된 사용자 ID

        if (loggedInUser != null) {
          // 작성자 확인
          log.info("qnaDetail: Qna userName: {}", qna.getUserName()); // Q&A 작성자 이름
          if (qna.getUserName() != null && qna.getUserName().equals(loggedInUser.getName())) {
            isAuthor = true;
            log.info("qnaDetail: User is author."); // 작성자 일치
          } else {
            log.info(
                "qnaDetail: User is NOT author. Qna userName: {}, LoggedInUser name: {}",
                qna.getUserName(),
                loggedInUser.getName()); // 작성자 불일치
          }
          // 관리자 확인
          if (loggedInUser.getRole().equals(Role.ADMIN)) {
            isAdmin = true;
            log.info("qnaDetail: User is admin."); // 관리자 일치
          } else {
            log.info(
                "qnaDetail: User is NOT admin. User role: {}", loggedInUser.getRole()); // 관리자 불일치
          }
        }
      }
      model.addAttribute("isAuthor", isAuthor);
      model.addAttribute("isAdmin", isAdmin);
      log.info("qnaDetail: isAuthor: {}, isAdmin: {}", isAuthor, isAdmin); // 최종 isAuthor, isAdmin 값

      return "board/qna/detail";
    } catch (IllegalAccessException e) {
      log.error(
          "qnaDetail: IllegalAccessException for Q&A ID {}: {}", qnaId, e.getMessage()); // 에러 로깅
      // 비밀글이고 권한이 없으면 접근 거부 메시지를 표시하도록 모델에 플래그 추가
      model.addAttribute("accessDenied", true);
      // 최소한의 QnaResDTO 객체를 생성하여 isSecret을 true로 설정 (템플릿 조건 처리를 위함)
      QnaResDTO dummyQna = new QnaResDTO();
      dummyQna.setIsSecret(true);
      model.addAttribute("qna", dummyQna);
      model.addAttribute("qnaId", qnaId); // qnaId도 함께 전달
      model.addAttribute("isAuthor", false); // 접근 권한이 없으므로 false
      model.addAttribute("isAdmin", false); // 접근 권한이 없으므로 false
      return "board/qna/detail";
    } catch (IllegalArgumentException e) {
      model.addAttribute("errorMessage", e.getMessage());
      return "common/error";
    }
  }

  // Q&A 삭제 API
  @DeleteMapping("/qna/{qnaId}")
  @ResponseBody
  public ResponseEntity<?> deleteQna(@PathVariable Long qnaId, Principal principal) {
    try {
      qnaService.deleteQna(qnaId, principal);
      return ResponseEntity.ok().body("Q&A가 성공적으로 삭제되었습니다.");
    } catch (IllegalAccessException | IllegalArgumentException e) {
      return ResponseEntity.badRequest().body("Q&A 삭제 실패: " + e.getMessage());
    } catch (IOException e) {
      return ResponseEntity.status(500).body("파일 삭제 중 오류가 발생했습니다: " + e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Q&A 삭제 중 알 수 없는 오류가 발생했습니다.");
    }
  }

  @GetMapping("/qna/form")
  public String qnaForm(Model model) {
    model.addAttribute("perfumes", perfumeService.getAllPerfumes());
    return "board/qna/form";
  }

  @GetMapping("/qna/edit/{qnaId}")
  public String editQnaForm(@PathVariable Long qnaId, Model model, Principal principal) {
    try {
      QnaResDTO qna = qnaService.getQnaById(qnaId, principal);
      model.addAttribute("qna", qna);
      model.addAttribute("perfumes", perfumeService.getAllPerfumes());
      return "board/qna/edit";
    } catch (IllegalAccessException e) {
      model.addAttribute("errorMessage", e.getMessage());
      return "common/error";
    }
  }

  @PostMapping("/qna/edit/{qnaId}")
  public String updateQna(
      @PathVariable Long qnaId,
      @Valid @ModelAttribute QnaReqDTO reqDto,
      BindingResult bindingResult,
      Principal principal,
      RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      redirectAttributes.addFlashAttribute(
          "org.springframework.validation.BindingResult.qnaReqDTO", bindingResult);
      redirectAttributes.addFlashAttribute("qnaReqDTO", reqDto);
      return "redirect:/board/qna/edit/" + qnaId;
    }
    try {
      qnaService.updateQna(qnaId, reqDto, principal);
      return "redirect:/board/qna/detail/" + qnaId;
    } catch (IOException | IllegalAccessException e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
      return "redirect:/board/qna/edit/" + qnaId;
    }
  }
}
