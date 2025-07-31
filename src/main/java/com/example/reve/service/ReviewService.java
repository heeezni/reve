package com.example.reve.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.reve.domain.Review;
import com.example.reve.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
  private final ReviewRepository reviewRepository;

  // 평점별 리뷰 카운트 가져오는 기능임.
  public Map<Integer, Long> getReviewCountsByRating(Long perfumeId) {
    List<Object[]> results = reviewRepository.countReviewsByRating(perfumeId);
    Map<Integer, Long> ratingCounts = new HashMap<>();

    // 결과 가공
    for (Object[] row : results) {
      Integer rating = ((Number) row[0]).intValue();
      Long count = (Long) row[1];
      ratingCounts.put(rating, count);
    }

    return ratingCounts;
  }

  public List<Review> getReviewsByPerfumeId(Long perfumeId) {
    return reviewRepository.findByPerfume_PerfumeIdOrderByCreatedAtDesc(perfumeId);
  }

  public List<Review> getReviewsWithUserByPerfumeId(Long perfumeId) {
    return reviewRepository.findByPerfume_PerfumeIdOrderByCreatedAtDesc(perfumeId);
  }
}
