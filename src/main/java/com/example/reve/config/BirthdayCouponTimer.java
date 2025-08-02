package com.example.reve.config;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.example.reve.service.CouponService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class BirthdayCouponTimer {
  private final CouponService couponService;

  // 서버 실행 시 1번만 실행
  @PostConstruct
  public void startCouponTimer() {
    // 주기적으로 실행하는 인터페이스->스레드 사용
    try (ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor()) {
      Runnable task =
          () -> {
            try {
              // 매달 1일에 실행
              if (LocalDate.now().getDayOfMonth() == 1) {
                log.info("당월 쿠폰 발급 시작");
                couponService.couponByMonth();
              }
            } catch (Exception e) {
              log.error("쿠폰 자동발급 오류{}", e.getMessage());
              throw new RuntimeException(e);
            }
          };
      long delayUntil = delayUntil();
      // 24시간 반복
      long oneday = TimeUnit.DAYS.toMinutes(1);

      // 일정 기간 반복
      scheduler.scheduleAtFixedRate(task, delayUntil, oneday, TimeUnit.MILLISECONDS);
    }
  }

  // 다음 2시까지 계산
  public long delayUntil() {
    LocalDateTime now = LocalDateTime.now();
    // 시간설정(새벽 2시)
    LocalDateTime startTime =
        LocalDate.now().atStartOfDay().withHour(2).withMinute(0).withSecond(0).withNano(0);
    // 지난 새벽 2시->다음 새벽 2시
    if (now.isAfter(startTime)) {
      startTime = startTime.plusDays(1);
    }
    // 현재 시간으로부터 다음 새벽 2시까지 시간 계산
    return Duration.between(now, startTime).toMillis();
  }
}
