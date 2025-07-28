package com.example.reve.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.reve.domain.Perfume;

public interface PerfumeRepository extends JpaRepository<Perfume, Long> {

  // 최신 등록일 기준으로 향수 검색
  List<Perfume> findAllByOrderByCreatedAtDesc();

  // 낮은 가격 순으로 향수 검색
  List<Perfume> findAllByOrderByPriceAsc();

  // 높은 가격 순으로 향수 검색
  List<Perfume> findAllByOrderByPriceDesc();

  // 리뷰 많은 순으로 향수 검색
  @Query(
      "SELECT p FROM Perfume p LEFT JOIN p.reviewList r GROUP BY p.perfumeId ORDER BY COUNT(r) DESC")
  List<Perfume> findAllOrderByReviewCountDesc();
}
