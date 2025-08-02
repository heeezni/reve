package com.example.reve.repository;

import java.time.LocalDateTime;
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

  // 이전 공지사항 가져오기 (중요도, 생성일, ID 고려)
  @Query(
      "SELECT n FROM Notice n WHERE "
          + "(n.important = :currentImportant AND n.createdAt < :currentCreatedAt) OR "
          + "(n.important = :currentImportant AND n.createdAt = :currentCreatedAt AND n.noticeId < :currentNoticeId) OR "
          + "(n.important < :currentImportant) " // 현재 공지가 중요(true)이고 이전 공지가 중요하지 않은(false) 경우
          + "ORDER BY n.important DESC, n.createdAt DESC, n.noticeId DESC FETCH FIRST 1 ROWS ONLY")
  Optional<Notice> findPrevNoticeByImportantAndCreatedAt(
      @Param("currentNoticeId") Long currentNoticeId,
      @Param("currentImportant") boolean currentImportant,
      @Param("currentCreatedAt") LocalDateTime currentCreatedAt);

  // 다음 공지사항 가져오기 (중요도, 생성일, ID 고려)
  @Query(
      "SELECT n FROM Notice n WHERE "
          + "(n.important = :currentImportant AND n.createdAt > :currentCreatedAt) OR "
          + "(n.important = :currentImportant AND n.createdAt = :currentCreatedAt AND n.noticeId > :currentNoticeId) OR "
          + "(n.important > :currentImportant) " // 현재 공지가 중요하지 않고(false) 다음 공지가 중요한(true) 경우
          + "ORDER BY n.important ASC, n.createdAt ASC, n.noticeId ASC FETCH FIRST 1 ROWS ONLY")
  Optional<Notice> findNextNoticeByImportantAndCreatedAt(
      @Param("currentNoticeId") Long currentNoticeId,
      @Param("currentImportant") boolean currentImportant,
      @Param("currentCreatedAt") LocalDateTime currentCreatedAt);

  // 현재 공지사항을 제외하고 같은 카테고리의 공지사항 목록을 최신순으로 가져오기
  @Query(
      "SELECT n FROM Notice n WHERE n.category = :category AND n.noticeId != :noticeId ORDER BY n.createdAt DESC")
  List<Notice> findRelatedNotices(
      @Param("category") String category, @Param("noticeId") Long noticeId, Pageable pageable);
}
