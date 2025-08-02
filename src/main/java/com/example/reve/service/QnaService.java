package com.example.reve.service;

import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.reve.domain.Perfume;
import com.example.reve.domain.Qna;
import com.example.reve.domain.Role;
import com.example.reve.domain.User;
import com.example.reve.dto.QnaReqDTO;
import com.example.reve.dto.QnaResDTO;
import com.example.reve.repository.PerfumeRepository;
import com.example.reve.repository.QnaRepository;
import com.example.reve.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QnaService {

  private final QnaRepository qnaRepository;
  private final UserRepository userRepository;
  private final PerfumeRepository perfumeRepository;
  private final FileUploadService fileUploadService;

  @Transactional
  public QnaResDTO createQna(QnaReqDTO reqDto, Principal principal) throws IOException {
    Qna savedQna = null; // 파일 업로드 전에 저장된 Qna 엔티티를 보관하기 위한 용도
    try {
      User user =
          userRepository
              .findByLoginId(principal.getName())
              .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

      Perfume perfume =
          perfumeRepository
              .findById(reqDto.getPerfumeId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "해당 상품을 찾을 수 없습니다. ID: " + reqDto.getPerfumeId()));

      Qna qna = new Qna();
      qna.setTitle(reqDto.getTitle());
      qna.setContent(reqDto.getContent());
      qna.setCategory(reqDto.getCategory());
      qna.setUser(user);
      qna.setPerfume(perfume);
      qna.setIsSecret(reqDto.getIsSecret());

      // 1. Qna 엔티티를 먼저 저장하여 ID 얻기
      savedQna = qnaRepository.save(qna);

      // 2. 첨부파일이 있다면, Qna ID를 사용하여 파일을 저장
      if (reqDto.getAttachmentFiles() != null && !reqDto.getAttachmentFiles().isEmpty()) {
        List<String> uploadedFilePaths =
            fileUploadService.saveFiles(
                reqDto.getAttachmentFiles(), "qna", String.valueOf(savedQna.getQnaId()));
        savedQna.setAttachment(String.join(",", uploadedFilePaths));
        qnaRepository.save(savedQna); // 파일 경로 업데이트 후 다시 저장
      }

      return new QnaResDTO(savedQna);
    } catch (IOException e) {
      // 파일 업로드 중 오류 발생 시, 이미 저장된 Qna 엔티티와 파일 디렉토리 정리
      if (savedQna != null && savedQna.getQnaId() != null) {
        try {
          fileUploadService.deleteDirectory("qna", String.valueOf(savedQna.getQnaId()));
          qnaRepository.delete(savedQna); // Qna 엔티티도 롤백 (트랜잭션이 이미 롤백될 수도 있지만 명시적으로)
        } catch (IOException deleteEx) {
          log.error("Q&A 생성 실패 후 디렉토리 삭제 중 오류 발생: {}", deleteEx.getMessage());
        }
      }
      log.error("Q&A 생성 중 파일 처리 오류 발생: {}", e.getMessage());
      throw e; // IOException을 그대로 던짐
    } catch (Exception e) {
      // 다른 일반적인 오류 발생 시, 이미 저장된 Qna 엔티티와 파일 디렉토리 정리
      if (savedQna != null && savedQna.getQnaId() != null) {
        try {
          fileUploadService.deleteDirectory("qna", String.valueOf(savedQna.getQnaId()));
          qnaRepository.delete(savedQna); // Qna 엔티티도 롤백
        } catch (IOException deleteEx) {
          log.error("Q&A 생성 실패 후 디렉토리 삭제 중 오류 발생: {}", deleteEx.getMessage());
        }
      }
      log.error("Q&A 생성 중 오류 발생: {}", e.getMessage());
      throw e; // 기존 예외 다시 던지기
    }
  }

  /**
   * Q&A 게시글 업데이트
   *
   * @param qnaId 업데이트할 Q&A ID
   * @param reqDto 업데이트할 Q&A 정보
   * @throws IOException 파일 처리 중 오류 발생 시
   */
  @Transactional
  public void updateQna(Long qnaId, QnaReqDTO reqDto, Principal principal)
      throws IOException, AccessDeniedException {
    Qna qna =
        qnaRepository
            .findById(qnaId)
            .orElseThrow(() -> new IllegalArgumentException("해당 Q&A를 찾을 수 없습니다. ID: " + qnaId));

    validateUserPermission(principal, qna);

    // Q&A 내용 업데이트
    qna.setTitle(reqDto.getTitle());
    qna.setContent(reqDto.getContent());
    qna.setCategory(reqDto.getCategory());
    qna.setIsSecret(reqDto.getIsSecret());

    // 관련 상품 업데이트
    Perfume perfume =
        perfumeRepository
            .findById(reqDto.getPerfumeId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException("해당 상품을 찾을 수 없습니다. ID: " + reqDto.getPerfumeId()));
    qna.setPerfume(perfume);

    // --- 파일 처리 로직 ---
    List<String> existingAttachments = new ArrayList<>();
    if (qna.getAttachment() != null && !qna.getAttachment().isEmpty()) {
      existingAttachments.addAll(Arrays.asList(qna.getAttachment().split(",")));
    }

    // 2. 삭제 요청된 파일 처리
    if (reqDto.getDeletedAttachments() != null) {
      for (String deletedUrl : reqDto.getDeletedAttachments()) {
        fileUploadService.deleteFile(deletedUrl);
        existingAttachments.remove(deletedUrl);
      }
    }

    // 3. 새로 추가된 파일 처리
    if (reqDto.getAttachmentFiles() != null && !reqDto.getAttachmentFiles().isEmpty()) {
      // 변경된 FileUploadService의 saveFiles 메소드를 사용하여 파일 저장
      List<String> newUploadedFilePaths =
          fileUploadService.saveFiles(reqDto.getAttachmentFiles(), "qna", String.valueOf(qnaId));
      // 새로 업로드된 파일 경로들을 기존 목록에 추가
      if (newUploadedFilePaths != null && !newUploadedFilePaths.isEmpty()) {
        existingAttachments.addAll(newUploadedFilePaths);
      }
    }

    // 최종 파일 목록을 콤마로 구분된 문자열로 변환하여 Qna 엔티티에 저장
    // 목록이 비어있으면 null로 설정
    String finalAttachments =
        existingAttachments.isEmpty() ? null : String.join(",", existingAttachments);
    qna.setAttachment(finalAttachments);

    Qna updatedQna = qnaRepository.save(qna);
    new QnaResDTO(updatedQna);
  }

  /**
   * 모든 Q&A 게시글을 최신순으로 페이징하여 조회하고 QnaResDTO Page로 반환
   *
   * @param pageable 페이징 정보 (페이지 번호, 페이지 크기, 정렬 등)
   * @return QnaResDTO의 Page 객체
   */
  public Page<QnaResDTO> selectAll(
      String keyword, String category, String status, Pageable pageable) {
    // QnaRepository를 사용하여 데이터베이스에서 Qna 엔티티를 페이징하여 가져오기
    // findAll(Pageable) 메소드는 Page<Qna>를 반환하며, 정렬 정보는 pageable에 포함되어 있음
    Page<Qna> qnaPage = qnaRepository.findFilteredQnas(keyword, category, status, pageable);

    // Page<Qna>를 Page<QnaResDTO>로 변환
    List<QnaResDTO> qnaResDtoList =
        qnaPage.getContent().stream().map(QnaResDTO::new).collect(Collectors.toList());

    return new PageImpl<>(qnaResDtoList, pageable, qnaPage.getTotalElements());
  }

  /**
   * Q&A 게시글의 접근 권한을 확인하고, 권한이 있는 경우 QnaResDTO를 반환
   *
   * @param qnaId Q&A ID
   * @param principal 현재 로그인한 사용자 정보
   * @return QnaResDTO 객체
   */
  public QnaResDTO getQnaById(Long qnaId, Principal principal) throws AccessDeniedException {
    Qna qna =
        qnaRepository
            .findById(qnaId)
            .orElseThrow(() -> new IllegalArgumentException("해당 Q&A를 찾을 수 없습니다. ID: " + qnaId));

    // 공개글이더라도 로그인하지 않은 사용자에게는 접근을 허용하지 않음
    if (principal == null) {
      throw new AccessDeniedException("로그인이 필요합니다.");
    }

    // 비밀글인 경우, 접근 권한 확인
    if (qna.getIsSecret()) {
      validateUserPermission(principal, qna);
    }

    Long prevQnaId = findAccessiblePrevQnaId(qnaId, principal);
    Long nextQnaId = findAccessibleNextQnaId(qnaId, principal);
    return new QnaResDTO(qna, prevQnaId, nextQnaId);
  }

  private Long findAccessiblePrevQnaId(Long currentQnaId, Principal principal) {
    Long prevId = currentQnaId;
    while (true) {
      Optional<Long> foundId = qnaRepository.findPrevQnaId(prevId);
      if (foundId.isEmpty()) {
        return null; // 더 이상 이전 글이 없음
      }
      prevId = foundId.get();
      Qna qna = qnaRepository.findById(prevId).orElse(null);
      if (qna != null) {
        if (!qna.getIsSecret()) { // 공개글이면 바로 반환
          return prevId;
        } else { // 비밀글이면 권한 확인
          validateUserPermission(principal, qna);
          return prevId; // 권한 있으면 반환
        }
      }
    }
  }

  private Long findAccessibleNextQnaId(Long currentQnaId, Principal principal) {
    Long nextId = currentQnaId;
    while (true) {
      Optional<Long> foundId = qnaRepository.findNextQnaId(nextId);
      if (foundId.isEmpty()) {
        return null; // 더 이상 다음 글이 없음
      }
      nextId = foundId.get();
      Qna qna = qnaRepository.findById(nextId).orElse(null);
      if (qna != null) {
        if (!qna.getIsSecret()) { // 공개글이면 바로 반환
          return nextId;
        } else { // 비밀글이면 권한 확인
          validateUserPermission(principal, qna);
          return nextId; // 권한 있으면 반환
        }
      }
    }
  }

  /**
   * Q&A 게시글 삭제
   *
   * @param qnaId 삭제할 Q&A ID
   * @throws IOException 파일 삭제 중 오류 발생 시
   */
  @Transactional
  public void deleteQna(Long qnaId, Principal principal) throws IOException, AccessDeniedException {
    Qna qna =
        qnaRepository
            .findById(qnaId)
            .orElseThrow(() -> new IllegalArgumentException("해당 Q&A를 찾을 수 없습니다. ID: " + qnaId));

    validateUserPermission(principal, qna);

    // 첨부 파일 디렉토리 삭제
    if (qna.getAttachment() != null && !qna.getAttachment().isEmpty()) {
      // 변경된 FileUploadService의 deleteDirectory 메소드를 사용하여 디렉토리 삭제
      fileUploadService.deleteDirectory("qna", String.valueOf(qna.getQnaId()));
    }

    qnaRepository.delete(qna);
  }

  /**
   * Q&A 게시글에 답변 추가 (관리자)
   *
   * @param qnaId 답변할 Q&A ID
   * @param answerContent 답변 내용
   * @param principal 현재 로그인한 사용자 정보 (관리자 권한 확인용)
   */
  @Transactional
  public void addAnswer(Long qnaId, String answerContent, Principal principal)
      throws AccessDeniedException {
    Qna qna =
        qnaRepository
            .findById(qnaId)
            .orElseThrow(() -> new IllegalArgumentException("해당 Q&A를 찾을 수 없습니다. ID: " + qnaId));

    validateAdminPermission(principal);

    qna.setAnswer(answerContent);
    Qna updatedQna = qnaRepository.save(qna);
    new QnaResDTO(updatedQna);
  }

  /**
   * Q&A 게시글 답변 수정
   *
   * @param qnaId 수정할 Q&A ID
   * @param updatedAnswerContent 수정된 답변 내용
   * @param principal 현재 로그인한 사용자 정보 (관리자 권한 확인용)
   */
  @Transactional
  public void updateAnswer(Long qnaId, String updatedAnswerContent, Principal principal)
      throws AccessDeniedException {
    Qna qna =
        qnaRepository
            .findById(qnaId)
            .orElseThrow(() -> new IllegalArgumentException("해당 Q&A를 찾을 수 없습니다. ID: " + qnaId));

    validateAdminPermission(principal);

    qna.setAnswer(updatedAnswerContent);
    Qna updatedQna = qnaRepository.save(qna);
    new QnaResDTO(updatedQna);
  }

  /**
   * Q&A 게시글 답변 삭제
   *
   * @param qnaId 삭제할 Q&A ID
   * @param principal 현재 로그인한 사용자 정보 (관리자 권한 확인용)
   */
  @Transactional
  public void deleteAnswer(Long qnaId, Principal principal) throws AccessDeniedException {
    Qna qna =
        qnaRepository
            .findById(qnaId)
            .orElseThrow(() -> new IllegalArgumentException("해당 Q&A를 찾을 수 없습니다. ID: " + qnaId));

    validateAdminPermission(principal);

    qna.setAnswer(null);
    qnaRepository.save(qna);
  }

  private void validateUserPermission(Principal principal, Qna qna) throws AccessDeniedException {
    if (principal == null) {
      throw new AccessDeniedException("로그인이 필요합니다.");
    }
    User user =
        userRepository
            .findByLoginId(principal.getName())
            .orElseThrow(() -> new AccessDeniedException("사용자 정보를 찾을 수 없습니다."));

    if (user.getRole() != Role.ADMIN && !user.getUserId().equals(qna.getUser().getUserId())) {
      throw new AccessDeniedException("해당 게시글에 대한 권한이 없습니다.");
    }
  }

  private void validateAdminPermission(Principal principal) throws AccessDeniedException {
    if (principal == null) {
      throw new AccessDeniedException("로그인이 필요합니다.");
    }
    User user =
        userRepository
            .findByLoginId(principal.getName())
            .orElseThrow(() -> new AccessDeniedException("사용자 정보를 찾을 수 없습니다."));

    if (user.getRole() != Role.ADMIN) {
      throw new AccessDeniedException("관리자 권한이 필요합니다.");
    }
  }
}
