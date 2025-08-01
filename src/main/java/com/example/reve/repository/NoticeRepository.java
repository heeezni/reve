package com.example.reve.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.reve.domain.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

  // 키워드가 포함된 공지사항을 찾는 쿼리 (Pageable 추가)
  Page<Notice> findByTitleContaining(String keyword, Pageable pageable);

  // 카테고리별 공지사항 조회 (Pageable 추가)
  Page<Notice> findByCategory(String category, Pageable pageable);
}
