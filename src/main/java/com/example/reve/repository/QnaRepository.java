package com.example.reve.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.reve.domain.Qna;

public interface QnaRepository extends JpaRepository<Qna, Long> {

  // 특정 유저가 작성한 Q&A 목록
  List<Qna> findByUser_UserId(Long userId);

  // 특정 유저 + 향수 조합으로 Q&A 목록 조회
  List<Qna> findByUser_UserIdAndPerfume_PerfumeId(Long userId, Long perfumeId);
}
