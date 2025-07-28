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
public class QnA extends BaseEntity {

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

  // Q&A 작성 비밀번호(Q&A 수정이나 삭제 시 필요)
  @Column(nullable = false)
  private String password;

  // Q&A 작성글에대한 답변
  @Column(nullable = true, columnDefinition = "TEXT")
  private String answer;

  // Q&A 작성 타입(비밀글인지 공개글인지)
  @Column(nullable = false)
  private Boolean isSecret = false;

  // 관계 설정
  // Q&A와 유저와의 관계 (N : 1)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // Q&A와 향수와의 관계 (N : 1)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "perfume_id", nullable = false)
  private Perfume perfume;
}
