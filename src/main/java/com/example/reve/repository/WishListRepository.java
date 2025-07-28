package com.example.reve.repository;

import com.example.reve.domain.WishList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishListRepository extends JpaRepository<WishList, Long> {

  // 특정 유저의 위시리스트 전체 조회
  List<WishList> findByUser_UserId(Long userId);

  // 특정 유저 + 향수 조합으로 위시리스트에 담았는지 조회
  Optional<WishList> findByUser_UserIdAndPerfume_PerfumeId(Long userId, Long perfumeId);

  // 향수를 찜한 총 유저 수 (찜 수)
  long countByPerfume_PerfumeId(Long perfumeId);
}
