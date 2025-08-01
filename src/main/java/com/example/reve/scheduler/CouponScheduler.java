package com.example.reve.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.reve.service.CouponService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponScheduler {
  private final CouponService couponService;

    @Scheduled(cron = "0 0 0 1 * *") //매달 1일 0:00 에 실행
//  @Scheduled(cron = "30 * * * * *") // 테스트용 30초마다 실행
  public void MonthlyCoupon() {
    log.info("======당월 생일자 쿠폰 발급 시작=====");
    couponService.couponByMonth();
    log.info("======당월 생일자 쿠폰 발급 완료=====");
  }
}
