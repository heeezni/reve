package com.example.reve.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.reve.domain.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

  // 특정 유저의 주문 전체 조회
  List<Order> findByUser_UserId(Long userId);

  // 주문 상태로 필터링 (예 : 배송완료)
  List<Order> findByOrderStatus(String orderStatus);

  // 최근 주문 내역 조회
  List<Order> findByUser_UserIdOrderByCreatedAtDesc(Long userId);
}
