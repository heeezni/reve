package com.example.reve.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import lombok.*;

/*
공지사항 엔터티
 */
@Entity
@Table(name = "notice")
@Getter
@Setter
@NoArgsConstructor
// @AllArgsConstructor // 이전에 추가했던 @AllArgsConstructor는 제거합니다.
// @Builder // 이전에 추가했던 클래스 레벨의 @Builder는 제거합니다.
public class Notice extends BaseEntity {

  // PK
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "notice_id")
  private Long noticeId;

  // 공지사항 카테고리
  @Column(nullable = false)
  private String category;

  // 공지사항 제목
  @Column(nullable = false)
  private String title;

  // 공지사항 내용
  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  // 중요 공지 여부
  @Column(nullable = false)
  private boolean important = false;

  // 공지사항 조회수
  @Column(nullable = false)
  private int hit = 0;

  // 첨부파일 경로
  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "notice_attachments", joinColumns = @JoinColumn(name = "notice_id"))
  @Column(name = "file_path")
  private List<String> attachmentFiles = new ArrayList<>();

  // 관계 매핑 : 공지사항은 유저와 N : 1
  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // 빌더를 위한 명시적 생성자 정의
  @Builder
  public Notice(
      String title,
      String content,
      String category,
      boolean important,
      User user,
      List<String> attachmentFiles) {
    this.title = title;
    this.content = content;
    this.category = category;
    this.important = important;
    this.user = user;
    if (attachmentFiles != null) {
      this.attachmentFiles = attachmentFiles;
    }
  }
}
