package com.example.reve.repository;

import com.example.reve.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

  // 특정 향수에 대한 리뷰 목록 조회 (최신순)
  List<Review> findByPerfume_PerfumeIdOrderByCreatedAtDesc(Long perfumeId);

  // 특정 향수의 평균 평점 계산을 위해 리뷰 모두 조회
  List<Review> findByPerfume_PerfumeId(Long perfumeId);
}
