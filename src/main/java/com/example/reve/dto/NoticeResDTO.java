package com.example.reve.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.reve.domain.Notice;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NoticeResDTO {
  private Long noticeId;
  private String category;
  private String title;
  private String content;
  private boolean important;
  private int hit;
  private List<String> attachmentFiles;
  private String userName;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public NoticeResDTO(Notice notice) {
    this.noticeId = notice.getNoticeId();
    this.category = notice.getCategory();
    this.title = notice.getTitle();
    this.content = notice.getContent();
    this.important = notice.isImportant();
    this.hit = notice.getHit();
    this.attachmentFiles = notice.getAttachmentFiles();
    this.userName = (notice.getUser() != null) ? notice.getUser().getName() : "알 수 없음";
    this.createdAt = notice.getCreatedAt();
    this.updatedAt = notice.getUpdatedAt();
  }
}
