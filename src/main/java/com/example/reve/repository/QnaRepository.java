package com.example.reve.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.reve.domain.Qna;

public interface QnaRepository extends JpaRepository<Qna, Long> {

  // 특정 유저가 작성한 Q&A 목록
  List<Qna> findByUser_UserId(Long userId);

  // 특정 유저 + 향수 조합으로 Q&A 목록 조회
  List<Qna> findByUser_UserIdAndPerfume_PerfumeId(Long userId, Long perfumeId);

  // 모든 Q&A를 최신순으로 정렬하여 조회
  @Query("SELECT q FROM Qna q ORDER BY q.createdAt DESC")
  List<Qna> findAllByLatest();

  // 현재 qnaId보다 작은 qnaId 중 가장 큰 값 (이전 글, 즉 목록에서 더 최신 글)
  @Query(
      value = "SELECT qna_id FROM qna WHERE qna_id < :qnaId ORDER BY qna_id DESC LIMIT 1",
      nativeQuery = true)
  Optional<Long> findPrevQnaId(@Param("qnaId") Long qnaId);

  // 현재 qnaId보다 큰 qnaId 중 가장 작은 값 (다음 글, 즉 목록에서 더 오래된 글)
  @Query(
      value = "SELECT qna_id FROM qna WHERE qna_id > :qnaId ORDER BY qna_id ASC LIMIT 1",
      nativeQuery = true)
  Optional<Long> findNextQnaId(@Param("qnaId") Long qnaId);
}
