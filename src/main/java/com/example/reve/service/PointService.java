package com.example.reve.service;

import org.springframework.stereotype.Service;

import com.example.reve.domain.Point;
import com.example.reve.domain.User;
import com.example.reve.repository.PointRepository;
import com.example.reve.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PointService {
  private final PointRepository pointRepository;
  private final UserRepository userRepository;

  // 적립금 발급
  public void getPoint(Long userId, int pointAmount) {
    log.info("적립급 발급 서비스 호출");
    User user = userRepository.findById(userId).orElseThrow();
    Point point = pointRepository.findByUser_UserId(userId).orElse(null);
    if (point == null) {
      point = new Point();
      point.setUser(user);
      point.setPointAmount(pointAmount);
      log.info("신규 회원 포인트 발급");
    } else {
      point.setPointAmount(point.getPointAmount() + pointAmount);
      log.info("기존 회원 포인트 발급");
    }
    pointRepository.save(point);
  }

  // 포인트 조회
  public Integer getPointAmount(String loginId) {
    User user = userRepository.findByLoginId(loginId).orElseThrow();
    log.info(
        "회원 {} 님이 소유한 적립급 : {}",
        user.getLoginId(),
        pointRepository.findPointAmountByUserId(user.getUserId()).orElse(0));
    return pointRepository.findPointAmountByUserId(user.getUserId()).orElse(0);
  }
}
