package com.example.reve.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.reve.domain.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

  // 키워드가 포함된 공지사항을 찾는 쿼리 (Pageable 추가)
  Page<Notice> findByTitleContaining(String keyword, Pageable pageable);

  // 카테고리별 공지사항 조회 (Pageable 추가)
  Page<Notice> findByCategory(String category, Pageable pageable);

  // 현재 공지사항보다 ID가 작은(이전) 공지사항 중 가장 큰 ID를 가진 공지사항
  @Query(
      "SELECT n FROM Notice n WHERE n.noticeId < :noticeId ORDER BY n.noticeId DESC FETCH FIRST 1 ROWS ONLY")
  Optional<Notice> findPrevNotice(@Param("noticeId") Long noticeId);

  // 현재 공지사항보다 ID가 큰(다음) 공지사항 중 가장 작은 ID를 가진 공지사항
  @Query(
      "SELECT n FROM Notice n WHERE n.noticeId > :noticeId ORDER BY n.noticeId ASC FETCH FIRST 1 ROWS ONLY")
  Optional<Notice> findNextNotice(@Param("noticeId") Long noticeId);

  // 현재 공지사항을 제외하고 같은 카테고리의 공지사항 목록을 최신순으로 가져오기
  @Query(
      "SELECT n FROM Notice n WHERE n.category = :category AND n.noticeId != :noticeId ORDER BY n.createdAt DESC")
  List<Notice> findRelatedNotices(
      @Param("category") String category, @Param("noticeId") Long noticeId, Pageable pageable);
}
