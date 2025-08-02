package com.example.reve.service;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.reve.domain.Notice;
import com.example.reve.domain.Role;
import com.example.reve.domain.User;
import com.example.reve.dto.NoticeReqDTO;
import com.example.reve.repository.NoticeRepository;
import com.example.reve.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {

  private final NoticeRepository noticeRepository;
  private final UserRepository userRepository;
  private final FileUploadService fileUploadService;

  @Transactional
  public void createNotice(NoticeReqDTO noticeReqDTO, Principal principal) throws IOException {
    Notice savedNotice = null; // 저장된 Notice 엔티티를 추적하기 위한 변수
    try {
      if (principal == null) {
        throw new IllegalArgumentException("로그인된 사용자만 공지사항을 등록할 수 있습니다.");
      }
      User author =
          userRepository
              .findByLoginId(principal.getName())
              .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

      // 관리자 역할 체크
      if (!author.getRole().equals(Role.ADMIN)) {
        throw new AccessDeniedException("관리자만 공지사항을 등록할 수 있습니다.");
      }

      String category = noticeReqDTO.getCategory();
      if (category == null || category.trim().isEmpty()) {
        category = "일반"; // 기본값 설정
      }

      // 1. Notice 엔티티를 먼저 저장하여 ID를 얻기
      Notice notice =
          Notice.builder()
              .title(noticeReqDTO.getTitle())
              .content(noticeReqDTO.getContent())
              .category(category)
              .important(noticeReqDTO.isImportant())
              .user(author)
              .build();
      savedNotice = noticeRepository.save(notice);

      // 2. 첨부파일이 있다면, Notice ID를 사용하여 파일을 저장
      if (noticeReqDTO.getAttachmentFiles() != null
          && !noticeReqDTO.getAttachmentFiles().isEmpty()) {
        List<String> uploadedFilePaths =
            fileUploadService.saveFiles(
                noticeReqDTO.getAttachmentFiles(),
                "notice",
                String.valueOf(savedNotice.getNoticeId()));
        savedNotice.setAttachmentFiles(uploadedFilePaths);
        noticeRepository.save(savedNotice); // 파일 경로 업데이트 후 다시 저장
      }

    } catch (IOException e) {
      // 파일 업로드 중 오류 발생 시, 이미 저장된 Notice 엔티티와 파일 디렉토리 정리
      if (savedNotice != null && savedNotice.getNoticeId() != null) {
        try {
          fileUploadService.deleteDirectory("notice", String.valueOf(savedNotice.getNoticeId()));
          noticeRepository.delete(savedNotice); // Notice 엔티티도 롤백
        } catch (IOException deleteEx) {
          log.error("공지사항 생성 실패 후 디렉토리 삭제 중 오류 발생: {}", deleteEx.getMessage());
        }
      }
      log.error("공지사항 생성 중 파일 처리 오류 발생: {}", e.getMessage());
      throw e; // IOException을 그대로 던짐
    } catch (Exception e) {
      // 다른 일반적인 오류 발생 시, 이미 저장된 Notice 엔티티와 파일 디렉토리 정리
      if (savedNotice != null && savedNotice.getNoticeId() != null) {
        try {
          fileUploadService.deleteDirectory("notice", String.valueOf(savedNotice.getNoticeId()));
          noticeRepository.delete(savedNotice); // Notice 엔티티도 롤백
        } catch (IOException deleteEx) {
          log.error("공지사항 생성 실패 후 디렉토리 삭제 중 오류 발생: {}", deleteEx.getMessage());
        }
      }
      log.error("공지사항 생성 중 오류 발생: {}", e.getMessage());
      throw e; // 기존 예외 다시 던지기
    }
  }

  // 모든 공지사항을 페이징하여 가져오는 메서드
  @Transactional(readOnly = true)
  public Page<Notice> getAllNotices(Pageable pageable, String keyword, String category) {
    if (keyword != null && !keyword.isEmpty()) {
      return noticeRepository.findByTitleContaining(keyword, pageable);
    } else if (category != null && !category.isEmpty()) {
      return noticeRepository.findByCategory(category, pageable);
    }
    return noticeRepository.findAll(pageable);
  }

  // 특정 ID의 공지사항을 가져오는 메서드
  @Transactional
  public Notice getNoticeById(Long noticeId) {
    Notice notice =
        noticeRepository
            .findById(noticeId)
            .orElseThrow(() -> new NoSuchElementException("공지사항을 찾을 수 없습니다: " + noticeId));
    notice.setHit(notice.getHit() + 1); // 새로고침마다 조회수+1
    return noticeRepository.save(notice);
  }

  // 이전 공지사항 가져오기
  @Transactional(readOnly = true)
  public Optional<Notice> getPrevNotice(Long currentNoticeId) {
    return noticeRepository.findPrevNotice(currentNoticeId);
  }

  // 다음 공지사항 가져오기
  @Transactional(readOnly = true)
  public Optional<Notice> getNextNotice(Long currentNoticeId) {
    return noticeRepository.findNextNotice(currentNoticeId);
  }

  // 관련 공지사항 가져오기 (현재 공지사항 제외, 같은 카테고리 내에서, 최신순)
  @Transactional(readOnly = true)
  public List<Notice> getRelatedNotices(Long currentNoticeId, String category, int limit) {
    Pageable pageable = PageRequest.of(0, limit);
    return noticeRepository.findRelatedNotices(category, currentNoticeId, pageable);
  }

  @Transactional
  public void deleteNotice(Long noticeId, Principal principal)
      throws AccessDeniedException, IOException {
    Notice notice =
        noticeRepository
            .findById(noticeId)
            .orElseThrow(() -> new NoSuchElementException("공지사항을 찾을 수 없습니다: " + noticeId));

    User currentUser =
        userRepository
            .findByLoginId(principal.getName())
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    // 관리자만 삭제 가능
    if (!currentUser.getRole().equals(Role.ADMIN)) {
      throw new AccessDeniedException("공지사항을 삭제할 권한이 없습니다.");
    }

    // 첨부파일 삭제
    if (notice.getAttachmentFiles() != null && !notice.getAttachmentFiles().isEmpty()) {
      // 변경된 FileUploadService의 deleteDirectory 메소드를 사용하여 디렉토리 삭제
      fileUploadService.deleteDirectory("notice", String.valueOf(noticeId));
    }

    noticeRepository.delete(notice);
  }

  @Transactional
  public Notice updateNotice(Long noticeId, NoticeReqDTO noticeReqDTO, Principal principal)
      throws IOException, AccessDeniedException {
    Notice notice =
        noticeRepository
            .findById(noticeId)
            .orElseThrow(() -> new NoSuchElementException("공지사항을 찾을 수 없습니다: " + noticeId));

    User currentUser =
        userRepository
            .findByLoginId(principal.getName())
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    if (!currentUser.getRole().equals(Role.ADMIN)) {
      throw new AccessDeniedException("공지사항을 수정할 권한이 없습니다.");
    }

    notice.setTitle(noticeReqDTO.getTitle());
    notice.setContent(noticeReqDTO.getContent());
    notice.setCategory(noticeReqDTO.getCategory());
    notice.setImportant(noticeReqDTO.isImportant());

    // 기존 파일 삭제 처리
    if (noticeReqDTO.getDeletedAttachmentFiles() != null
        && !noticeReqDTO.getDeletedAttachmentFiles().isEmpty()) {
      for (String filePath : noticeReqDTO.getDeletedAttachmentFiles()) {
        try {
          fileUploadService.deleteFile(filePath);
          notice.getAttachmentFiles().remove(filePath);
        } catch (IOException e) {
          System.err.println("기존 첨부파일 삭제 실패: " + filePath + " - " + e.getMessage());
        }
      }
    }

    // 새 파일 추가 처리
    if (noticeReqDTO.getNewAttachmentFiles() != null
        && !noticeReqDTO.getNewAttachmentFiles().isEmpty()) {
      List<String> newUploadedFilePaths =
          fileUploadService.saveFiles(
              noticeReqDTO.getNewAttachmentFiles(), "notice", String.valueOf(noticeId));
      if (notice.getAttachmentFiles() == null) {
        notice.setAttachmentFiles(newUploadedFilePaths);
      } else {
        notice.getAttachmentFiles().addAll(newUploadedFilePaths);
      }
    }

    return noticeRepository.save(notice);
  }
}
