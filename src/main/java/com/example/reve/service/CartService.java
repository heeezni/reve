package com.example.reve.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

  // 로그인 ID로 장바구니 아이템 목록 조회
  @Transactional(readOnly = true)
  public List<Cart> getCartItemsByLoginId(String loginId) {
    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

    return cartRepository.findByUser_UserId(user.getUserId());
  }

  @Transactional(readOnly = true)
  public int countDistinctItems(String loginId) {
    User user =
        userRepository
            .findByLoginId(loginId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

    // 장바구니에 담긴 서로 다른 향수 종류 수 반환 (리스트 크기)
    return cartRepository.findByUser_UserId(user.getUserId()).size();
  }

  public List<Perfume> getRecommendedPerfumes(List<Cart> cartList) {
    Set<String> scentSet =
        cartList.stream().map(cart -> cart.getPerfume().getScent()).collect(Collectors.toSet());

    List<Long> excludeIds =
        cartList.stream()
            .map(cart -> cart.getPerfume().getPerfumeId())
            .collect(Collectors.toList());

    Pageable top3 = PageRequest.of(0, 3);

    return perfumeRepository.findRecommendedPerfumes(scentSet, excludeIds, top3);
  }

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
      // 중복일 경우 예외 던지기
      throw new IllegalStateException("이미 장바구니에 담긴 상품입니다.");
    } else {
      // 없으면 새로 저장
      Cart newCart = Cart.builder().user(user).perfume(perfume).quantity(quantity).build();
      cartRepository.save(newCart);
    }

    return countDistinctItems(loginId);
  }

  @Transactional
  public void changeQuantity(Long cartId, int quantity) {
    Cart cart =
        cartRepository
            .findById(cartId)
            .orElseThrow(() -> new IllegalArgumentException("장바구니 항목이 없습니다."));
    if (quantity < 1) {
      throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
    }
    cart.setQuantity(quantity);
    cartRepository.save(cart);
  }

  public void removeAllCartItemsByLoginId(String loginId) {
    // 로그인 ID로 해당 사용자의 장바구니 항목 전체 삭제 로직 작성
    cartRepository.deleteAllByUserLoginId(loginId);
  }

  // 장바구니 아이템 삭제
  public void removeCartItem(Long cartId) {
    cartRepository.deleteById(cartId);
  }
}
