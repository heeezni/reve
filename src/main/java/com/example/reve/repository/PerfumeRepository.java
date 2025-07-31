package com.example.reve.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.reve.domain.Perfume;

/*
향수와 관련된 JPA를 선언하는 레퍼지토리
 */
public interface PerfumeRepository extends JpaRepository<Perfume, Long> {

  // 검색어로 향수 검색
  @Query("SELECT p FROM Perfume p WHERE LOWER(p.perfumeName) LIKE LOWER(CONCAT('%', :search, '%'))")
  List<Perfume> findByPerfumeNameContainingIgnoreCase(String search);

  // 향 기준으로 향수 검색
  List<Perfume> findAllByScent(String scent);

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

  // 향 + 최신순으로 필터링
  List<Perfume> findByScentOrderByCreatedAtDesc(String scent);

  // 향 + 낮은 가격순으로 필터링
  List<Perfume> findByScentOrderByPriceAsc(String scent);

  // 향 + 높은 가격순으로 필터링
  List<Perfume> findByScentOrderByPriceDesc(String scent);

  // 향 + 리뷰 많은 순으로 필터링
  @Query(
      "SELECT p FROM Perfume p LEFT JOIN p.reviewList r WHERE p.scent = :scent GROUP BY p.perfumeId ORDER BY COUNT(r) DESC")
  List<Perfume> findByScentOrderByReviewCountDesc(String scent);

  // 향수 아이디로 단건 조회
  Optional<Perfume> findByPerfumeId(Long perfumeId);

  // 같은 향의 향수를 페이징 처리하여 조회
  Page<Perfume> findByScent(String scent, Pageable pageable);

  // 검색 + 페이징 + 정렬
  @Query(
      "SELECT p FROM Perfume p LEFT JOIN p.reviewList r "
          + "WHERE (:search IS NULL OR LOWER(p.perfumeName) LIKE LOWER(CONCAT('%', :search, '%'))) "
          + "AND (:scent IS NULL OR p.scent = :scent) "
          + "GROUP BY p.perfumeId")
  Page<Perfume> findBySearchAndScentWithReviewJoin(
      @Param("search") String search, @Param("scent") String scent, Pageable pageable);
}
