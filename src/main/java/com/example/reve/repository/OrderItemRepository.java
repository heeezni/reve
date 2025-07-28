package com.example.reve.repository;

import com.example.reve.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

  // 특정 주문의 상세 항목 전체 조회
  List<OrderItem> findByOrder_OrderId(Long orderId);

  // 특정 향수가 포함된 주문항목 조회
  List<OrderItem> findByPerfume_PerfumeId(Long perfumeId);
}
