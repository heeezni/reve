package com.example.reve.service;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.reve.domain.Notice;
import com.example.reve.domain.Role;
import com.example.reve.domain.User;
import com.example.reve.dto.NoticeReqDTO;
import com.example.reve.repository.NoticeRepository;
import com.example.reve.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeService {

  private final NoticeRepository noticeRepository;
  private final UserRepository userRepository;
  private final FileUploadService fileUploadService;

  @Transactional
  public Notice createNotice(NoticeReqDTO noticeReqDTO, Principal principal) {
    if (principal == null) {
      throw new IllegalArgumentException("로그인된 사용자만 공지사항을 등록할 수 있습니다.");
    }
    User author =
        userRepository
            .findByLoginId(principal.getName())
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    // 관리자 역할 체크 추가
    if (!author.getRole().equals(Role.ADMIN)) {
      throw new IllegalArgumentException("관리자만 공지사항을 등록할 수 있습니다.");
    }

    String category = noticeReqDTO.getCategory();
    if (category == null || category.trim().isEmpty()) {
      category = "일반"; // 기본값 설정
    }

    // 파일 업로드 및 경로 저장
    List<String> uploadedFilePaths = new ArrayList<>();
    if (noticeReqDTO.getAttachmentFiles() != null && !noticeReqDTO.getAttachmentFiles().isEmpty()) {
      for (MultipartFile file : noticeReqDTO.getAttachmentFiles()) {
        if (!file.isEmpty()) {
          try {
            String filePath = fileUploadService.saveFile(file, "notice"); // 'notice' 폴더에 저장
            uploadedFilePaths.add(filePath);
          } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패: " + e.getMessage(), e);
          }
        }
      }
    }

    Notice notice =
        Notice.builder()
            .title(noticeReqDTO.getTitle())
            .content(noticeReqDTO.getContent())
            .category(category)
            .important(noticeReqDTO.isImportant())
            .user(author)
            .attachmentFiles(uploadedFilePaths)
            .build();

    return noticeRepository.save(notice);
  }

  // 모든 공지사항을 페이징하여 가져오는 메서드
  @Transactional(readOnly = true)
  public Page<Notice> getAllNotices(Pageable pageable, String keyword, String category) {
    Sort sort =
        Sort.by(Sort.Direction.DESC, "important").and(Sort.by(Sort.Direction.DESC, "createdAt"));
    Pageable sortedPageable =
        PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

    if (keyword != null && !keyword.isEmpty()) {
      return noticeRepository.findByTitleContaining(keyword, sortedPageable);
    } else if (category != null && !category.isEmpty()) {
      return noticeRepository.findByCategory(category, sortedPageable);
    }
    return noticeRepository.findAll(sortedPageable);
  }

  // 특정 ID의 공지사항을 가져오는 메서드
  @Transactional(readOnly = true)
  public Notice getNoticeById(Long noticeId) {
    return noticeRepository
        .findById(noticeId)
        .orElseThrow(() -> new NoSuchElementException("공지사항을 찾을 수 없습니다: " + noticeId));
  }

  @Transactional
  public void deleteNotice(Long noticeId, Principal principal) throws IllegalAccessException {
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
      throw new IllegalAccessException("공지사항을 삭제할 권한이 없습니다.");
    }

    // 첨부파일 삭제
    if (notice.getAttachmentFiles() != null && !notice.getAttachmentFiles().isEmpty()) {
      for (String filePath : notice.getAttachmentFiles()) {
        try {
          fileUploadService.deleteFile(filePath);
        } catch (IOException e) {
          // 파일 삭제 실패 시 로그만 남기고 계속 진행
          System.err.println("첨부파일 삭제 실패: " + filePath + " - " + e.getMessage());
        }
      }
    }

    noticeRepository.delete(notice);
  }
}
