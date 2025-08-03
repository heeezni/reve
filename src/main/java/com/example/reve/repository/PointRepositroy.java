package com.example.reve.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.reve.domain.Point;

public interface PointRepositroy extends JpaRepository<Point, Long> {
  // 유저가 가지고 있는 포인트 조회
  Optional<Point> findByUser_UserId(Long userId);
}
