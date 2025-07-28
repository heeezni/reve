package com.example.reve.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.reve.domain.QnA;

public interface QnARepository extends JpaRepository<QnA, Long> {

  // 특정 유저가 작성한 Q&A 목록
  List<QnA> findByUser_UserId(Long userId);

  // 특정 유저 + 향수 조합으로 Q&A 목록 조회
  List<QnA> findByUser_UserIdAndPerfume_PerfumeId(Long userId, Long perfumeId);
}
