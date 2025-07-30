package com.example.reve.dto;

import java.time.LocalDateTime;

import com.example.reve.domain.Qna;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QnaResDto {
  private Long qnaId;
  private String title;
  private String content;
  private String answer;
  private Boolean isSecret;
  private String category;
  private String attachment;
  private String userName;
  private String perfumeName;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public QnaResDto(Qna qna) {
    this.qnaId = qna.getQnaId();
    this.title = qna.getTitle();
    this.content = qna.getContent();
    this.answer = qna.getAnswer();
    this.isSecret = qna.getIsSecret();
    this.category = qna.getCategory();
    this.attachment = qna.getAttachment();
    this.userName = (qna.getUser() != null) ? qna.getUser().getName() : "비회원";
    this.perfumeName = qna.getPerfume().getPerfumeName();
    this.createdAt = qna.getCreatedAt();
    this.updatedAt = qna.getUpdatedAt();
  }
}
