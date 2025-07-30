package com.example.reve.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.reve.domain.Perfume;
import com.example.reve.domain.Qna;
import com.example.reve.domain.User;
import com.example.reve.dto.QnaReqDto;
import com.example.reve.dto.QnaResDto;
import com.example.reve.repository.PerfumeRepository;
import com.example.reve.repository.QnaRepository;
import com.example.reve.repository.UserRepository;

import lombok.RequiredArgsConstructor;

// MultipartFile import 추가

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QnaService {

  private final QnaRepository qnaRepository;
  private final UserRepository userRepository;
  private final PerfumeRepository perfumeRepository;
  private final FileUploadService fileUploadService; // FileUploadService 주입

  @Transactional
  public QnaResDto createQna(QnaReqDto reqDto) {
    // 파일 업로드 실패 시 롤백을 위해 임시 디렉토리 이름 생성
    String tempDirectoryName = UUID.randomUUID().toString();

    try {
      // 1. DTO에서 받은 userId로 진짜 유저가 DB에 있는지 찾기
      Optional<User> userOptional = userRepository.findById(reqDto.getUserId());
      User user; // 나중에 사용될 User 객체를 미리 선언

      // 2. Optional 안에 User가 있는지 확인
      if (userOptional.isPresent()) {
        // User 있으면, 꺼내서 user 변수에 담기
        user = userOptional.get();
      } else {
        // 없으면 에러를 발생시키기
        throw new IllegalArgumentException("해당 유저를 찾을 수 없습니다. ID: " + reqDto.getUserId());
      }

      // 3. perfumeId로 진짜 상품이 DB에 있는지 확인
      Optional<Perfume> perfumeOptional = perfumeRepository.findById(reqDto.getPerfumeId());
      Perfume perfume;

      // 4. Optional 안에 Perfume 있는지 확인
      if (perfumeOptional.isPresent()) {
        // Perfume 있으면, 꺼내서 perfume 변수에 담기
        perfume = perfumeOptional.get();
      } else {
        // 없으면 에러를 발생시키기
        throw new IllegalArgumentException("해당 상품을 찾을 수 없습니다. ID: " + reqDto.getPerfumeId());
      }

      // 5. 유저와 상품 정보가 있으면, Qna 게시글 생성
      Qna qna = new Qna();

      qna.setTitle(reqDto.getTitle());
      qna.setContent(reqDto.getContent());
      qna.setPassword(reqDto.getPassword());
      qna.setIsSecret(reqDto.getIsSecret());
      qna.setCategory(reqDto.getCategory());
      qna.setUser(user); // 위에서 찾은 유저 정보
      qna.setPerfume(perfume); // 위에서 찾은 상품 정보

      // 6. 첨부 파일 임시 디렉토리에 저장 및 경로 설정
      String savedAttachmentUrls = null;
      if (reqDto.getAttachmentFiles() != null && !reqDto.getAttachmentFiles().isEmpty()) {
        savedAttachmentUrls =
            fileUploadService.saveFiles(reqDto.getAttachmentFiles(), tempDirectoryName);
        qna.setAttachment(savedAttachmentUrls); // Qna 엔티티의 attachment 필드에 임시 경로 저장
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
      return new QnaResDto(savedQna);
    } catch (IOException e) {
      // 파일 저장/이름 변경 중 오류 발생 시 임시 디렉토리 삭제
      try {
        fileUploadService.deleteDirectory(tempDirectoryName);
      } catch (IOException deleteEx) {
        System.err.println("임시 디렉토리 삭제 중 오류 발생: " + deleteEx.getMessage());
      }
      System.err.println("이미지 파일 처리 중 오류 발생: " + e.getMessage());
      throw new RuntimeException("이미지 파일 처리 중 오류가 발생했습니다.", e);
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

  /** 모든 Q&A 게시글을 최신순으로 조회하여 QnaResDto 리스트로 반환 */
  public List<QnaResDto> selectAll() {
    // 1. QnaRepository를 사용하여 데이터베이스에서 모든 Qna 엔티티를 최신순으로 가져오기
    List<Qna> qnaList = qnaRepository.findAllByLatest();

    // 2. QnaResDto 객체들을 담을 빈 리스트 만들기
    List<QnaResDto> qnaResDtoList = new ArrayList<>();

    // 3. 데이터베이스에서 가져온 qnaList의 각 Qna 객체를 순회하며 살펴보기
    for (Qna qna : qnaList) {
      // 4. 현재 살펴보고 있는 qna를 가지고 새로운 QnaResDto 객체 만들기
      QnaResDto qnaResDto = new QnaResDto(qna);

      // 5. 새로 만들어진 QnaResDto 객체를, 아까 만들어둔 빈 리스트에 추가
      qnaResDtoList.add(qnaResDto);
    }

    // 6. 모든 Qna 객체를 QnaResDto로 변환하여 담은 리스트 반환
    return qnaResDtoList;
  }
}
