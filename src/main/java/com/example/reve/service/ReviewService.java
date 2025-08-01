package com.example.reve.service;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.reve.domain.Perfume;
import com.example.reve.domain.Review;
import com.example.reve.domain.User;
import com.example.reve.dto.ReviewCreateDTO;
import com.example.reve.repository.PerfumeRepository;
import com.example.reve.repository.ReviewRepository;
import com.example.reve.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/*
리뷰 관련 로직을 처리하는 서비스임.
 */
@Service
@RequiredArgsConstructor
public class ReviewService {
  private final ReviewRepository reviewRepository;
  private final UserRepository userRepository;
  private final PerfumeRepository perfumeRepository;

  @Value("${file.upload-dir}")
  private String uploadDir;

  @Value("${file.image-url-prefix}")
  private String imageUrlPrefix;

  // 리뷰를 생성해주는 로직임.
  public void createReview(ReviewCreateDTO dto, Principal principal) {
    // 사용자 찾기
    User user =
        userRepository
            .findByLoginId(principal.getName())
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    // 향수 찾기
    Perfume perfume =
        perfumeRepository
            .findById(Long.valueOf(dto.getPerfumeId()))
            .orElseThrow(() -> new IllegalArgumentException("향수를 찾을 수 없습니다."));

    String imageUrl = null;
    MultipartFile imageFile = dto.getImageUrl();
    if (imageFile != null && !imageFile.isEmpty()) {
      imageUrl = storeImage(imageFile);
    }

    Review review =
        Review.builder()
            .content(dto.getContent())
            .rating(dto.getRating())
            .imageUrl(imageUrl)
            .user(user)
            .perfume(perfume)
            .build();

    reviewRepository.save(review);
  }

  // 이미지를 저장해주는 로직임.
  public String storeImage(MultipartFile file) {
    if (file.isEmpty()) {
      throw new IllegalArgumentException("빈 파일은 저장할 수 없습니다.");
    }

    try {
      String originalFilename = file.getOriginalFilename();
      String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
      String uniqueFilename = UUID.randomUUID().toString() + extension;

      File directory = new File(uploadDir);
      if (!directory.exists()) {
        directory.mkdirs();
      }

      File saveFile = new File(uploadDir, uniqueFilename);
      file.transferTo(saveFile);

      return imageUrlPrefix + uniqueFilename;
    } catch (IOException e) {
      throw new RuntimeException("이미지 저장 실패", e);
    }
  }

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

  // 해당 향수에 리뷰 목록을 가져오는 로직임.
  public List<Review> getReviewsByPerfumeId(Long perfumeId) {
    return reviewRepository.findByPerfume_PerfumeIdOrderByCreatedAtDesc(perfumeId);
  }

  // 해당 향수별 유저가 작성한 리뷰 목록을 가져오는 로직임.
  public List<Review> getReviewsWithUserByPerfumeId(Long perfumeId) {
    return reviewRepository.findByPerfume_PerfumeIdOrderByCreatedAtDesc(perfumeId);
  }
}
