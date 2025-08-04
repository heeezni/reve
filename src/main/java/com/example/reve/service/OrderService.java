package com.example.reve.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.reve.domain.User;
import com.example.reve.dto.GetOrderDTO;
import com.example.reve.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.reve.domain.Order;
import com.example.reve.domain.OrderItem;
import com.example.reve.repository.OrderItemRepository;
import com.example.reve.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final UserRepository userRepository;

  /** 주문 생성 */
  @Transactional
  public Order createOrder(Order order) {
    // 주문번호가 없을 경우에만 생성
    if (order.getOrderNumber() == null || order.getOrderNumber().isEmpty()) {
      String orderNumber = generateOrderNumber();
      order.setOrderNumber(orderNumber);
    }

    // 기본 상태 설정
    if (order.getOrderStatus() == null) {
      order.setOrderStatus(Order.OrderStatus.ORDERED);
    }
    if (order.getPaymentStatus() == null) {
      order.setPaymentStatus(Order.PaymentStatus.PENDING);
    }

    return orderRepository.save(order);
  }

  /** 주문번호 생성 (ORD-YYYYMMDD-XXXX 형식) */
  private String generateOrderNumber() {
    String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
    return "ORD-" + dateStr + "-" + timeStr;
  }

  /** 주문 조회 (ID) */
  public Optional<Order> findOrderById(Long orderId) {
    return orderRepository.findById(orderId);
  }

  /** 사용자별 주문 목록 조회 (OrderItem 포함) */
  public List<Order> findOrdersByUserId(Long userId) {
    List<Order> orders = orderRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);

    // 각 주문에 대해 OrderItem 목록을 로드
    for (Order order : orders) {
      List<OrderItem> orderItems = getOrderItemsByOrderId(order.getOrderId());
      order.setOrderItems(orderItems);
    }

    return orders;
  }

  /** 주문 통계 - 상태별 개수 */
  public List<Object[]> getOrderStatusCount() {
    return orderRepository.countByOrderStatus();
  }

  /** 오늘 주문 개수 */
  public Long getTodayOrderCount() {
    return orderRepository.countTodayOrders();
  }

  /** 이번 달 주문 개수 */
  public Long getThisMonthOrderCount() {
    return orderRepository.countThisMonthOrders();
  }

  /** 주문 상품 추가 */
  @Transactional
  public OrderItem addOrderItem(OrderItem orderItem) {
    return orderItemRepository.save(orderItem);
  }

  /** 주문별 상품 목록 조회 */
  public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
    return orderItemRepository.findByOrderOrderId(orderId);
  }

  /** 주문 상태 업데이트 */
  @Transactional
  public void updateOrderStatus(Long orderId, String orderStatus) {
    Optional<Order> orderOpt = orderRepository.findById(orderId);
    if (orderOpt.isPresent()) {
      Order order = orderOpt.get();
      order.setOrderStatus(orderStatus);
      orderRepository.save(order);
      return;
    }
    throw new RuntimeException("주문을 찾을 수 없습니다: " + orderId);
  }

  /** 결제 상태 업데이트 */
  @Transactional
  public void updatePaymentStatus(Long orderId, String paymentStatus) {
    Optional<Order> orderOpt = orderRepository.findById(orderId);
    if (orderOpt.isPresent()) {
      Order order = orderOpt.get();
      order.setPaymentStatus(paymentStatus);
      orderRepository.save(order);
      return;
    }
    throw new RuntimeException("주문을 찾을 수 없습니다: " + orderId);
  }

  //주문내역 조회하기
  public List<GetOrderDTO>getAllOrders(String loginId) {
    User user= userRepository.findByLoginId(loginId).orElseThrow();
    List<Order> orders = orderRepository.findByUser_UserIdOrderByCreatedAtDesc(user.getUserId());
    List<GetOrderDTO> dtoList = new ArrayList<>();

    for (Order order : orders) {
      GetOrderDTO dto = new GetOrderDTO(); // DTO 객체 생성

      // 엔티티에서 DTO로 값 복사
      dto.setOrderNumber(order.getOrderNumber());
      dto.setCreateAt(order.getCreatedAt());
      dto.setTotalPrice(order.getTotalPrice());

      // 주문 항목이 있다면 첫 번째 향수 객체를 DTO에 넣음
      List<OrderItem> items = order.getOrderItems();
      if (items != null && !items.isEmpty()) {
        OrderItem firstItem = items.get(0);
        dto.setPerfume(firstItem.getPerfume());
      }

      // 리스트에 추가
      dtoList.add(dto);
    }
    return dtoList;
  }
}
