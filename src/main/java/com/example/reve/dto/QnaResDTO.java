package com.example.reve.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.reve.domain.Qna;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class QnaResDTO {
  private Long qnaId;
  private String title;
  private String content;
  private String answer;
  private Boolean isSecret;
  private Boolean isAnswered;
  private String category;
  private List<String> attachmentFiles;
  private String userName;
  private String perfumeName;
  private Long perfumeId; // 상품 ID
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Long prevQnaId; // 이전 글 ID
  private Long nextQnaId; // 다음 글 ID

  public QnaResDTO(Qna qna) {
    this.qnaId = qna.getQnaId();
    this.title = qna.getTitle();
    this.content = qna.getContent();
    this.answer = qna.getAnswer();
    this.isSecret = qna.getIsSecret();
    this.isAnswered = qna.getAnswer() != null && !qna.getAnswer().trim().isEmpty();
    this.category = qna.getCategory();
    this.attachmentFiles = qna.getAttachmentFiles(); // Qna 엔티티의 attachmentFiles 사용
    this.userName = (qna.getUser() != null) ? qna.getUser().getName() : "비회원";
    this.perfumeName = qna.getPerfume().getPerfumeName();
    this.perfumeId = qna.getPerfume().getPerfumeId();
    this.createdAt = qna.getCreatedAt();
    this.updatedAt = qna.getUpdatedAt();
  }

  // 이전/다음 글 ID를 설정하는 생성자 추가
  public QnaResDTO(Qna qna, Long prevQnaId, Long nextQnaId) {
    this(qna); // 기존 생성자 호출
    this.prevQnaId = prevQnaId;
    this.nextQnaId = nextQnaId;
  }
}
