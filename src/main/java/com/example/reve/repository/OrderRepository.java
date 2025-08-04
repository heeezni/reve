package com.example.reve.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.reve.domain.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  // 사용자 ID로 주문 목록 조회 (최신순)
  List<Order> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

  // 주문 상태별 개수 조회
  @Query("SELECT o.orderStatus, COUNT(o) FROM Order o GROUP BY o.orderStatus")
  List<Object[]> countByOrderStatus();

  // 오늘 주문 개수 조회
  @Query("SELECT COUNT(o) FROM Order o WHERE DATE(o.createdAt) = CURRENT_DATE")
  Long countTodayOrders();

  // 이번 달 주문 개수 조회
  @Query(
      "SELECT COUNT(o) FROM Order o WHERE YEAR(o.createdAt) = YEAR(CURRENT_DATE) AND MONTH(o.createdAt) = MONTH(CURRENT_DATE)")
  Long countThisMonthOrders();
}
