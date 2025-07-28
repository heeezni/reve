package com.example.reve.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;

/*
모든 엔터티의 공통 속성을 정의하는 엔터티임.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

  // 생성시간
  @Column(name = "created_at")
  @CreatedDate
  private LocalDateTime createdAt;

  // 수정시간
  @Column(name = "updated_at")
  @LastModifiedDate
  private LocalDateTime updatedAt;
}
