package com.example.reve.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.reve.domain.Point;

public interface PointRepository extends JpaRepository<Point, Long> {
  // 유저가 가지고 있는 포인트 조회
  Optional<Point> findByUser_UserId(Long userId);

  @Query("SELECT p.pointAmount FROM Point p WHERE p.user.userId = :userId")
  Optional<Integer> findPointAmountByUserId(@Param("userId") Long userId);

  @Query("SELECT COALESCE(SUM(p.pointAmount), 0) FROM Point p WHERE p.user.userId = :userId")
  int getTotalPointByUserId(@Param("userId") Long userId);
}
