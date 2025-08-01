package com.example.reve.domain;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "qna")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
  private Boolean isSecret = false;

  // Q&A 문의 분류 (상품문의, 배송문의, 결제문의 등)
  @Column(nullable = false)
  private String category;

  // Q&A 첨부 파일 경로 (여러 개일 경우 콤마로 구분)
  @Column(nullable = true, columnDefinition = "TEXT")
  private String attachment;

  // 관계 설정
  // Q&A와 유저와의 관계 (N : 1)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = true)
  private User user;

  // Q&A와 향수와의 관계 (N : 1)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "perfume_id", nullable = false)
  private Perfume perfume;
}
