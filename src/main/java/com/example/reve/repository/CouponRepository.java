package com.example.reve.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.reve.domain.Coupon;
import com.example.reve.domain.CouponName;
import com.example.reve.domain.User;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

  // 쿠폰 존제 여부 반환
  boolean existsByUserAndCouponNameAndIssuedAtAndValidFrom(
      User user, CouponName couponName, LocalDate issuedAt, LocalDate validFrom);

  // 쿠폰 코드로 조회
  Optional<Coupon> findByCode(String code);

  // 특정 유저가 가지고 있는 쿠폰 목록
  List<Coupon> findByUser_UserId(Long userId);

  // 아직 사용되지 않는 쿠폰만 조회
  List<Coupon> findByIsUsedFalse();
}
