package com.example.reve.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.reve.domain.*;
import com.example.reve.dto.WishListShowDTO;
import com.example.reve.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
찜목록 로직을 처리하는 서비스임.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WishListService {

  private final WishListRepository wishListRepository;
  private final PerfumeRepository perfumeRepository;
  private final ReviewRepository reviewRepository;
  private final UserRepository userRepository;
  private final CartRepository cartRepository;

  // 찜목록에 추가하거나 삭제하는 로직임.
  @Transactional
  public boolean toggleWish(User user, Long perfumeId) {
    Perfume perfume =
        perfumeRepository
            .findById(perfumeId)
            .orElseThrow(() -> new RuntimeException("향수를 찾을 수 없습니다"));

    Optional<WishList> wish =
        wishListRepository.findByUser_UserIdAndPerfume_PerfumeId(user.getUserId(), perfumeId);

    if (wish.isPresent()) {
      wishListRepository.delete(wish.get()); // 찜 해제
      return false; // 해제됨
    } else {
      wishListRepository.save(WishList.builder().user(user).perfume(perfume).build()); // 찜 추가
      return true; // 추가됨
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

  // 찜 상품 삭제
  public void wishperfumeDelete(Long perfumeId, String loginId) {
    log.info("찜 상품 삭제 서비스 호출");
    User user = userRepository.findByLoginId(loginId).orElseThrow();
    Optional<WishList> wish =
        wishListRepository.findByUser_UserIdAndPerfume_PerfumeId(user.getUserId(), perfumeId);
    if (wish.isPresent()) {
      wishListRepository.delete(wish.get());
      log.info("삭제 성공");
    }
  }

  // 찜 상품들 삭제
  public void wishlistDelete(List<Long> perfumeIds, String loginId) {
    log.info("선택한 찜상품들 삭제 서비스 호출");
    User user = userRepository.findByLoginId(loginId).orElseThrow();

    List<WishList> wishLists = wishListRepository.findByUser_UserId(user.getUserId());

    List<WishList> toDelete =
        wishLists.stream()
            .filter(wish -> perfumeIds.contains(wish.getPerfume().getPerfumeId()))
            .collect(Collectors.toList());

    wishListRepository.deleteAll(toDelete);
    log.info("선택된 찜상품들 삭제 완료");
  }

  // 카트 추가 서비스
  public boolean addCart(Long perfumeId, String loginId) {
    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new IllegalArgumentException("로그인이 필요합니다"));
    Optional<Cart> checkCart =
        cartRepository.findByUser_UserIdAndPerfume_PerfumeId(user.getUserId(), perfumeId);
    Perfume perfume =
        perfumeRepository
            .findByPerfumeId(perfumeId)
            .orElseThrow(() -> new IllegalArgumentException("상품이 없습니다"));
    if (checkCart.isPresent()) {
      return false;
    } else {
      Cart cart = new Cart();
      cart.setPerfume(perfume);
      cart.setUser(user);
      cart.setQuantity(1);
      cartRepository.save(cart);
      return true;
    }
  }
}
