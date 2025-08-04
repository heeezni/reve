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
import com.example.reve.dto.CartItem;
import com.example.reve.repository.CartRepository;
import com.example.reve.repository.PerfumeRepository;
import com.example.reve.repository.UserRepository;

import lombok.RequiredArgsConstructor;

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

  /** 사용자의 장바구니에서 구매한 상품들을 삭제 (새로운 기능) */
  public void removePurchasedItems(String userId, List<CartItem> purchasedItems) {
    try {
      // 사용자 ID로 Long 타입 변환
      Long userLongId = Long.parseLong(userId);

      // 사용자의 장바구니 목록 조회
      List<Cart> userCartItems = cartRepository.findByUser_UserId(userLongId);

      // 구매한 상품들을 장바구니에서 제거
      for (CartItem purchasedItem : purchasedItems) {
        // 상품명으로 해당 상품을 장바구니에서 찾아서 삭제
        userCartItems.removeIf(
            cartItem -> {
              if (cartItem.getPerfume() != null && cartItem.getPerfume().getPerfumeName() != null) {
                return cartItem.getPerfume().getPerfumeName().equals(purchasedItem.getName())
                    && cartItem.getQuantity().equals(purchasedItem.getQuantity());
              }
              return false;
            });
      }

      // DB에서 해당 상품들 삭제
      for (CartItem purchasedItem : purchasedItems) {
        // 상품명으로 해당 상품을 찾아서 삭제
        List<Cart> itemsToRemove = cartRepository.findByUser_UserId(userLongId);
        for (Cart cartItem : itemsToRemove) {
          if (cartItem.getPerfume() != null
              && cartItem.getPerfume().getPerfumeName() != null
              && cartItem.getPerfume().getPerfumeName().equals(purchasedItem.getName())
              && cartItem.getQuantity().equals(purchasedItem.getQuantity())) {
            cartRepository.delete(cartItem);
            break; // 첫 번째 일치하는 항목만 삭제
          }
        }
      }

    } catch (Exception e) {
      // 로그 출력
      System.out.println("장바구니에서 상품 제거 중 오류: " + e.getMessage());
    }
  }
}
