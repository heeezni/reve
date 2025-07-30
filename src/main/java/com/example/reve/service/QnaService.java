package com.example.reve.service;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
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
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public QnaResDTO createQna(QnaReqDTO reqDto) throws IOException {

    log.debug("QnaService - createQna: Received userId = {}", reqDto.getUserId());

    // 파일 업로드 실패 시 롤백을 위해 임시 디렉토리 이름 생성
    String tempDirectoryName = UUID.randomUUID().toString();

    try {
      // 1. DTO에서 받은 userId로 진짜 유저가 DB에 있는지 찾기
      User user = null;
      if (reqDto.getUserId() != null) {
        user =
            userRepository
                .findById(reqDto.getUserId())
                .orElseThrow(
                    () ->
                        new IllegalArgumentException(
                            "해당 유저를 찾을 수 없습니다. ID: " + reqDto.getUserId()));
      }

      // 3. perfumeId로 진짜 상품이 DB에 있는지 확인
      Perfume perfume =
          perfumeRepository
              .findById(reqDto.getPerfumeId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "해당 상품을 찾을 수 없습니다. ID: " + reqDto.getPerfumeId()));

      // 5. 유저와 상품 정보가 있으면, Qna 게시글 생성
      Qna qna = new Qna();
      qna.setTitle(reqDto.getTitle());
      qna.setContent(reqDto.getContent());
      qna.setCategory(reqDto.getCategory());
      qna.setUser(user);
      qna.setPerfume(perfume);

      // isSecret 값은 reqDto에서 받은 대로 사용
      qna.setIsSecret(reqDto.getIsSecret());

      // 6. 비밀글일 경우 비밀번호 암호화하여 저장
      if (qna.getIsSecret()) {
        if (reqDto.getPassword() == null || reqDto.getPassword().isEmpty()) {
          throw new IllegalArgumentException("비밀글은 비밀번호가 필수입니다.");
        }
        qna.setPassword(passwordEncoder.encode(reqDto.getPassword()));
      } else {
        qna.setPassword(null); // 공개글일 경우 비밀번호는 null
      }

      String savedAttachmentUrls = null;
      boolean hasAttachment =
          reqDto.getAttachmentFiles() != null && !reqDto.getAttachmentFiles().isEmpty();
      if (hasAttachment) {
        savedAttachmentUrls =
            fileUploadService.saveFiles(reqDto.getAttachmentFiles(), tempDirectoryName);
        qna.setAttachment(savedAttachmentUrls);
      }

      // 7. 만들어진 Qna 게시글을 DB에 저장 (PK를 얻기 위함)
      Qna savedQna = qnaRepository.save(qna);

      // 8. DB 저장 성공 후, 임시 디렉토리 이름을 실제 Q&A ID로 변경
      String newDirectoryName = "qna_" + savedQna.getQnaId();
      if (savedAttachmentUrls != null) { // 첨부파일이 업로드된 경우에만 디렉토리 이름 변경
        fileUploadService.renameDirectory(tempDirectoryName, newDirectoryName);
        // Qna 엔티티의 attachment 필드에 저장된 경로를 실제 경로로 업데이트
        String updatedAttachmentUrls =
            savedAttachmentUrls.replace(tempDirectoryName, newDirectoryName);
        savedQna.setAttachment(updatedAttachmentUrls);
        qnaRepository.save(savedQna); // 업데이트된 Qna 엔티티 다시 저장
      }

      // 9. 저장된 최종 결과를 바탕으로 클라이언트에게 보여줄 응답(DTO)을 만들어 반환
      return new QnaResDTO(savedQna);
    } catch (IOException e) { // RuntimeException 대신 IOException을 직접 처리
      // 파일 저장/이름 변경 중 오류 발생 시 임시 디렉토리 삭제
      try {
        fileUploadService.deleteDirectory(tempDirectoryName);
      } catch (IOException deleteEx) {
        System.err.println("임시 디렉토리 삭제 중 오류 발생: " + deleteEx.getMessage());
      }
      System.err.println("이미지 파일 처리 중 오류 발생: " + e.getMessage());
      throw e; // IOException을 그대로 던짐
    } catch (Exception e) {
      // 다른 일반적인 오류 발생 시 임시 디렉토리 삭제
      try {
        fileUploadService.deleteDirectory(tempDirectoryName);
      } catch (IOException deleteEx) {
        System.err.println("임시 디렉토리 삭제 중 오류 발생: " + deleteEx.getMessage());
      }
      System.err.println("Q&A 생성 중 오류 발생: " + e.getMessage());
      throw e; // 기존 예외 다시 던지기
    }
  }

  /**
   * 모든 Q&A 게시글을 최신순으로 페이징하여 조회하고 QnaResDTO Page로 반환
   *
   * @param pageable 페이징 정보 (페이지 번호, 페이지 크기, 정렬 등)
   * @return QnaResDTO의 Page 객체
   */
  public Page<QnaResDTO> selectAll(Pageable pageable) {
    // QnaRepository를 사용하여 데이터베이스에서 Qna 엔티티를 페이징하여 가져오기
    // findAll(Pageable) 메소드는 Page<Qna>를 반환하며, 정렬 정보는 pageable에 포함되어 있음
    Page<Qna> qnaPage = qnaRepository.findAll(pageable);

    // Page<Qna>를 Page<QnaResDTO>로 변환
    List<QnaResDTO> qnaResDtoList =
        qnaPage.getContent().stream().map(QnaResDTO::new).collect(Collectors.toList());

    return new PageImpl<>(qnaResDtoList, pageable, qnaPage.getTotalElements());
  }

  /**
   * Q&A 게시글의 접근 권한을 확인하고, 권한이 있는 경우 QnaResDTO를 반환한다.
   *
   * @param qnaId Q&A ID
   * @param principal 현재 로그인한 사용자 정보
   * @param password 입력된 비밀번호 (없을 경우 null)
   * @return QnaResDTO 객체
   * @throws IllegalAccessException 접근 권한이 없는 경우
   */
  public QnaResDTO getQnaById(Long qnaId, Principal principal, String password)
      throws IllegalAccessException {
    Qna qna =
        qnaRepository
            .findById(qnaId)
            .orElseThrow(() -> new IllegalArgumentException("해당 Q&A를 찾을 수 없습니다. ID: " + qnaId));

    // 공개글이면 바로 반환
    if (!qna.getIsSecret()) {
      return new QnaResDTO(qna);
    }

    // 비밀글인 경우, 접근 권한 확인
    // 1. 관리자인지 확인
    if (principal != null) {
      String loggedInUsername = principal.getName();
      boolean isAdmin =
          userRepository
              .findByLoginId(loggedInUsername)
              .map(user -> user.getRole().equals(Role.ADMIN))
              .orElse(false);
      if (isAdmin) {
        return new QnaResDTO(qna); // 관리자면 접근 허용
      }
    }

    // 2. 비밀번호 확인 (관리자가 아닌 경우)
    if (password == null) {
      throw new IllegalAccessException("비밀글입니다. 비밀번호를 입력해주세요.");
    }
    if (!passwordEncoder.matches(password, qna.getPassword())) {
      throw new IllegalAccessException("비밀번호가 일치하지 않습니다.");
    }

    // 비밀번호가 일치하면 접근 허용
    return new QnaResDTO(qna);
  }
}
