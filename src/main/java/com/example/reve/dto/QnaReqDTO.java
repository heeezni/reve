package com.example.reve.dto;

import java.util.List;

import jakarta.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

/** Q&A 게시글을 등록할 때 클라이언트에서 보낸 데이터를 담을 클래스 */
@Getter
@Setter
public class QnaReqDTO {
  @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
  private String title; // QnA 제목

  @Size(max = 2000, message = "내용은 2000자를 초과할 수 없습니다.")
  private String content; // QnA 내용

  private Boolean isSecret; // 비밀글 여부
  private String category; // QnA 문의 분류
  private String loginId; // 작성자 로그인 ID
  private Long perfumeId; // 관련 상품 ID

  @Size(max = 5, message = "최대 5개의 파일만 첨부할 수 있습니다.")
  private List<MultipartFile> attachmentFiles; // 첨부 파일 리스트

  private List<String> deletedAttachments; // 삭제할 첨부 파일 URL 리스트
}
