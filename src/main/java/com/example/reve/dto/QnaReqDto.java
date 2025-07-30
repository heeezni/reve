package com.example.reve.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

/** Q&A 게시글을 등록할 때 클라이언트에서 보낸 데이터를 담을 클래스 */
@Getter
@Setter
public class QnaReqDto {
  private String title; // QnA 제목
  private String content; // QnA 내용
  private String password; // QnA 비밀번호
  private Boolean isSecret; // 비밀글 여부
  private String category; // QnA 문의 분류
  private Long userId; // 작성자 ID
  private Long perfumeId; // 관련 상품 ID
  private List<MultipartFile> attachmentFiles; // 첨부 파일 리스트
}
