package com.example.reve.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "qna")
@Getter
@Setter
@NoArgsConstructor
@Builder
public class Qna extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "qna_id")
  private Long qnaId;

  // Q&A 제목
  @Column(nullable = false)
  private String title;

  // Q&A 내용
  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  // Q&A 작성글에 대한 답변
  @Column(nullable = true, columnDefinition = "TEXT")
  private String answer;

  // Q&A 작성 타입(비밀글인지 공개글인지)
  @Column(nullable = false)
  @Builder.Default // @Builder 사용 시 기본값 설정을 위해 추가
  private Boolean isSecret = false;

  // Q&A 문의 분류 (상품문의, 배송문의, 결제문의 등)
  @Column(nullable = false)
  private String category;

  // Q&A 첨부 파일 경로 (기존 String에서 List<String>으로 변경, 별도 테이블로 관리)
  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "qna_attachments", joinColumns = @JoinColumn(name = "qna_id"))
  @Column(name = "file_path")
  @Builder.Default // @Builder 사용 시 기본값 설정을 위해 추가
  private List<String> attachmentFiles = new ArrayList<>();

  // 관계 설정
  // Q&A와 유저와의 관계 (N : 1)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = true)
  private User user;

  // Q&A와 향수와의 관계 (N : 1)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "perfume_id", nullable = false)
  private Perfume perfume;

  @Builder
  public Qna(
      Long qnaId,
      String title,
      String content,
      String answer,
      Boolean isSecret,
      String category,
      List<String> attachmentFiles,
      User user,
      Perfume perfume) {
    this.qnaId = qnaId;
    this.title = title;
    this.content = content;
    this.answer = answer;
    this.isSecret = isSecret;
    this.category = category;
    this.attachmentFiles = attachmentFiles;
    this.user = user;
    this.perfume = perfume;
  }
}
