package com.example.reve.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.reve.domain.OrderItem;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

  // 주문 ID로 주문 상품 목록 조회
  List<OrderItem> findByOrderOrderId(Long orderId);
}
