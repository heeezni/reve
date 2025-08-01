package com.example.reve.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.reve.domain.Cart;
import com.example.reve.domain.Perfume;
import com.example.reve.domain.User;
import com.example.reve.repository.CartRepository;
import com.example.reve.repository.PerfumeRepository;
import com.example.reve.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/*
장바구니 관련 로직을 처리하는 서비스임.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

  private final CartRepository cartRepository;
  private final UserRepository userRepository;
  private final PerfumeRepository perfumeRepository;

  // 장바구니에 아이템 추가
  public int addToCart(String loginId, Long perfumeId, int quantity) {
    // 로그인된 유저 조회
    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

    // 향수 조회
    Perfume perfume =
        perfumeRepository
            .findById(perfumeId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 향수입니다."));

    // 이미 담긴 장바구니 아이템 확인
    Optional<Cart> existingCartOpt =
        cartRepository.findByUser_UserIdAndPerfume_PerfumeId(user.getUserId(), perfumeId);

    if (existingCartOpt.isPresent()) {
      // 있으면 수량 더하기
      Cart existingCart = existingCartOpt.get();
      existingCart.setQuantity(existingCart.getQuantity() + quantity);
      cartRepository.save(existingCart);
    } else {
      // 없으면 새로 저장
      Cart newCart = Cart.builder().user(user).perfume(perfume).quantity(quantity).build();
      cartRepository.save(newCart);
    }

    // 유저의 장바구니 총 아이템 개수 반환 (합산)
    int totalCount =
        cartRepository.findByUser_UserId(user.getUserId()).stream()
            .mapToInt(Cart::getQuantity)
            .sum();

    return totalCount;
  }
}
