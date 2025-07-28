package com.example.reve.domain;

import jakarta.persistence.*;

import lombok.*;

/*
자유롭게 유저들간 작성할 수 있는 게시판 엔터티임.
 */
@Entity
@Table(name = "board")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Board extends BaseEntity {

  // PK
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "board_id")
  private Long boardId;

  // 게시판 카테고리
  @Column(nullable = false)
  private String category;

  // 게시글 제목
  @Column(nullable = false)
  private String title;

  // 게시글 내용
  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  // 게시글 조회수
  @Column(nullable = false)
  private int hit = 0;

  // 게시글 비밀번호(게시글 수정이나 삭제시 필요)
  @Column(name = "board_password", nullable = false)
  private String boardPassword;

  // 관계 매핑 : 게시판은 유저와 N : 1
  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
}
