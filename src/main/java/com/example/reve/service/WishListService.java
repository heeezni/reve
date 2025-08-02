package com.example.reve.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.reve.domain.Perfume;
import com.example.reve.domain.User;
import com.example.reve.domain.WishList;
import com.example.reve.repository.PerfumeRepository;
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
}
