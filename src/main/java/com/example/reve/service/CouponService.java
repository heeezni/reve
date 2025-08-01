package com.example.reve.service;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.reve.domain.Coupon;
import com.example.reve.domain.CouponName;
import com.example.reve.domain.User;
import com.example.reve.dto.CouponDTO;
import com.example.reve.repository.CouponRepository;
import com.example.reve.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

  private final CouponRepository couponRepository;
  private final UserRepository userRepository;
  private Coupon coupon;

  //신규가입 쿠폰
  public  void  newUserCoupon(Long userId){
    LocalDate issuedAt = LocalDate.now();
    LocalDate validFrom = LocalDate.now();
    LocalDate expiresDate = validFrom.plusDays(3);
    User user = userRepository.findById(userId).orElseThrow();
    // 쿠폰 발급
    coupon.setCode("12341234");
    coupon.setUser(user);
    coupon.setCouponName(CouponName.BirthDay);
    coupon.setDiscountRate(10);
    coupon.setExpiresAt(expiresDate.atTime(23, 59, 59));
    coupon.setIsUsed(false);
    coupon.setIssuedAt(issuedAt);
    coupon.setValidFrom(validFrom);
    couponRepository.save(coupon);
    log.info("신규가입 쿠폰{}", coupon);

  }

  // 생일 등록 시 첫번째로 조회 후 발급되는 생일쿠폰
  public void birthdayCoupon(User user) {
    LocalDate today = LocalDate.now();
    LocalDate issuedAt = LocalDate.now();
    LocalDate birthday = null;
    LocalDate expiresDate = null;
    LocalDate validFrom = null;
    if (user.getBirthday() != null) {
      try {
        birthday = LocalDate.parse(user.getBirthday());
        expiresDate = birthday.plusDays(6);
        validFrom = birthday;
        // 2월 29일 보장
        if (birthday.getMonthValue() == 2 && birthday.getDayOfMonth() == 29) {
          boolean isLeap = Year.isLeap(today.getYear());
          if (!isLeap) {
            birthday = LocalDate.of(today.getYear(), 2, 28);
          } else {
            birthday = birthday.withYear(today.getYear());
          }
        } else {
          birthday = birthday.withYear(today.getYear());
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else {
      log.error("생일이 등록되어 있지 않음");
    }

    // 오늘 생일 확인
    if (!today.equals(birthday)) {
      log.info("오늘이 생일이 아님");
      return;
    }

    // 쿠폰 발급
    coupon = new Coupon();
    saveCoupon(coupon, user, expiresDate, validFrom, issuedAt);
  }

  // 기본 회원들의 생일 조회 후  발급하기
  @Transactional
  public void couponByMonth() {
    LocalDate today = LocalDate.now();
    LocalDate issuedAt = LocalDate.now();
    LocalDate birthday ;
    LocalDate expiresDate;
    LocalDate validFrom ;

    int month = today.getMonthValue();
    List<User> userList = userRepository.findAll();
    for (User user : userList) {
      if (user.getBirthday() != null) {
        birthday = LocalDate.parse(user.getBirthday());
        if (birthday.getMonthValue() != month) {
          continue;
        }
        LocalDate getYear = adjustLeapYearBirthday(birthday, today.getYear());
        expiresDate = getYear.plusDays(6);
        validFrom = birthday;
        // 쿠폰 발급
        coupon = new Coupon();
        saveCoupon(coupon, user, expiresDate, validFrom, issuedAt);
        log.info("기존 회원들 생일 조회 후 발급 성공 {}", user);
      }
    }
  }

  // 쿠폰 조회
  public List<CouponDTO> getCoupon(Long userId) {
    // 필요한 쿠폰 정보를 담을 리스트
    List<CouponDTO> result = new ArrayList<>();
    // 사용자가 가지고 있는 쿠폰들의 리스트
    List<Coupon> coupons = couponRepository.findByUser_UserId(userId);
    log.info("회원이 가지고 있는 쿠폰 : {}", coupons);
    for (Coupon coupon : coupons) {
      CouponDTO couponDTO = new CouponDTO();
      // 쿠폰명
      couponDTO.setCouponName(coupon.getCouponName());
      // 쿠폰 만료일
      couponDTO.setExpiresAt(coupon.getExpiresAt());
      // 사용가능여부
      couponDTO.setIsUsed(coupon.getIsUsed());
      // 쿠폰 유효 시작일
      couponDTO.setValidFrom(coupon.getValidFrom());
      // 쿠폰들 담기
      result.add(couponDTO);
    }
    return result;
  }

  // 쿠폰 발급 메서드
  public void saveCoupon(
      Coupon coupon, User user, LocalDate expiresDate, LocalDate validFrom, LocalDate issuedAt) {
    // 중복발급 확인
    boolean issued =
        couponRepository.existsByUserAndCouponNameAndIssuedAtAndValidFrom(
            user, CouponName.BirthDay, validFrom, issuedAt);
    if (issued) {
      log.info("발급 받은 회원");
      return;
    }

    // 쿠폰 발급
    coupon.setCode(user.getBirthday() + user.getPhone());
    coupon.setUser(user);
    coupon.setCouponName(CouponName.BirthDay);
    coupon.setDiscountRate(50);
    coupon.setExpiresAt(expiresDate.atTime(23, 59, 59));
    coupon.setIsUsed(false);
    coupon.setIssuedAt(issuedAt);
    coupon.setValidFrom(validFrom);
    couponRepository.save(coupon);
    log.info("첫번째 생일 등록 시 쿠폰 발급 하기{}", coupon);
  }

  // 2월 29일 생일자 설정
  private LocalDate adjustLeapYearBirthday(LocalDate birthday, int year) {
    if (birthday.getMonthValue() == 2 && birthday.getDayOfMonth() == 29) {
      return Year.isLeap(year) ? LocalDate.of(year, 2, 29) : LocalDate.of(year, 2, 28);
    }
    return birthday.withYear(year);
  }
}
