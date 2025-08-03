package com.example.reve.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.reve.domain.Perfume;
import com.example.reve.domain.Review;
import com.example.reve.domain.User;
import com.example.reve.domain.WishList;
import com.example.reve.dto.WishListShowDTO;
import com.example.reve.repository.PerfumeRepository;
import com.example.reve.repository.ReviewRepository;
import com.example.reve.repository.UserRepository;
import com.example.reve.repository.WishListRepository;

import lombok.RequiredArgsConstructor;

/*
찜목록 로직을 처리하는 서비스임.
 */
@Service
@RequiredArgsConstructor
public class WishListService {

  private final WishListRepository wishListRepository;
  private final PerfumeRepository perfumeRepository;
  private final ReviewRepository reviewRepository;
  private final UserRepository userRepository;

  // 찜목록에 추가하거나 삭제하는 로직임.
  @Transactional
  public void toggleWish(User user, Long perfumeId) {
    Perfume perfume =
        perfumeRepository
            .findById(perfumeId)
            .orElseThrow(() -> new RuntimeException("향수를 찾을 수 없습니다"));

    Optional<WishList> wish =
        wishListRepository.findByUser_UserIdAndPerfume_PerfumeId(user.getUserId(), perfumeId);

    if (wish.isPresent()) {
      wishListRepository.delete(wish.get()); // 찜 해제
    } else {
      wishListRepository.save(WishList.builder().user(user).perfume(perfume).build()); // 찜 추가
    }
  }

  // 찜목록에 있는지 없는지 확인하는 로직임.
  public boolean isWished(User user, Long perfumeId) {
    return wishListRepository
        .findByUser_UserIdAndPerfume_PerfumeId(user.getUserId(), perfumeId)
        .isPresent();
  }

  // 상품목록에서 찜목록 확인하기
  public List<Long> findWishListIdsByUser(User user) {
    List<WishList> wishList = wishListRepository.findByUser_UserId(user.getUserId());
    return wishList.stream()
        .map(wish -> wish.getPerfume().getPerfumeId())
        .collect(Collectors.toList());
  }

  // 위시리스트 목록 보기
  public List<WishListShowDTO> getWishList(String loginId) {
    User user = userRepository.findByLoginId(loginId).orElseThrow();
    Long userId = user.getUserId();
    List<WishListShowDTO> getWishList = new ArrayList<>();
    // 특정 회원이 가지고 있는 위시리스트
    List<WishList> wishList = wishListRepository.findByUser_UserId(userId);

    for (WishList wish : wishList) {
      Long perfumeId = wish.getPerfume().getPerfumeId();
      Perfume perfume = perfumeRepository.findByPerfumeId(perfumeId).orElse(null);
      int countReview =
          reviewRepository.findByPerfume_PerfumeIdOrderByCreatedAtDesc(perfumeId).size();
      double ratingAvg = starAvg(perfume);

      WishListShowDTO wishListShowDTO = new WishListShowDTO();
      wishListShowDTO.setPerfumeId(perfumeId);
      wishListShowDTO.setHoverImageUrl(perfume.getHoverImageUrl());
      wishListShowDTO.setPerfumeName(perfume.getPerfumeName());
      wishListShowDTO.setDiscount(perfume.getDiscount());
      wishListShowDTO.setPrice(perfume.getPrice());
      wishListShowDTO.setScent(perfume.getScent());
      wishListShowDTO.setDesciptionTitle(perfume.getDescriptionTitle());
      wishListShowDTO.setVolume(perfume.getVolume());
      wishListShowDTO.setRatingAvg(ratingAvg);
      wishListShowDTO.setCountReview(countReview);
      getWishList.add(wishListShowDTO);
    }
    return getWishList;
  }

  // 별 평균
  public double starAvg(Perfume perfume) {
    // 리뷰에 관한 로직임.
    List<Review> reviewList = perfume.getReviewList();
    int count = reviewList.size();
    double avgRating = 0.0;

    if (count > 0) {
      double sum = reviewList.stream().mapToDouble(Review::getRating).sum();
      avgRating = Math.round((sum / count) * 10) / 10.0; // 소수점 1자리 반올림
    }
    return avgRating;
  }

  // 찜 상품 개수 보기
  public int getCount(String loginId) {
    User user = userRepository.findByLoginId(loginId).orElseThrow();
    return wishListRepository.findByUser_UserId(user.getUserId()).size();
  }
}
