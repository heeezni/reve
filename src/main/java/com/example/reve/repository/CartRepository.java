package com.example.reve.repository;

import com.example.reve.domain.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

  // 특정 유저의 장바구니 전체 조회
  List<Cart> findByUser_UserId(Long userId);

  // 유저 + 향수 조합으로 이미 담은 항목이 있는지 확인 (중복 방지용)
  Optional<Cart> findByUser_UserIdAndPerfume_PerfumeId(Long userId, Long perfumeId);
}
