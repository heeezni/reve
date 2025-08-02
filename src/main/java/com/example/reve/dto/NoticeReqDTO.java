package com.example.reve.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeReqDTO {

  @NotBlank(message = "제목은 필수 입력 항목입니다.")
  @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
  private String title;

  @NotBlank(message = "내용은 필수 입력 항목입니다.")
  @Size(max = 2000, message = "내용은 2000자를 초과할 수 없습니다.")
  private String content;

  private String category; // 예: 일반, 이벤트, 시스템, 배송 등
  private boolean important; // 중요 공지 여부

  private List<MultipartFile> attachmentFiles;

  private List<String> deletedAttachmentFiles;
  private List<MultipartFile> newAttachmentFiles;
}
