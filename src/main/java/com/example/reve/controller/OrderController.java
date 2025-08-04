package com.example.reve.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.reve.domain.CustomUserDetails;
import com.example.reve.domain.Order;
import com.example.reve.domain.OrderItem;
import com.example.reve.dto.CartItem;
import com.example.reve.service.CartService;
import com.example.reve.service.OrderService;
import com.example.reve.service.WishListService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;
  private final CartService cartService;
  private final WishListService wishListService;
  private final ObjectMapper objectMapper = new ObjectMapper();

  /** 주문/결제 페이지 */
  @GetMapping("/checkout")
  public String checkout(
      @RequestParam(value = "items", required = false) String itemsJson,
      @RequestParam(value = "totalAmount", required = false) String totalAmount,
      @RequestParam(value = "deliveryFee", required = false) String deliveryFee,
      @RequestParam(value = "discountAmount", required = false) String discountAmount,
      @RequestParam(value = "itemCount", required = false) String itemCount,
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      Model model) {

    // 테스트용 데이터 (파라미터가 없을 경우)
    String orderId = "ORD-" + System.currentTimeMillis();
    String finalTotalAmount = totalAmount != null ? totalAmount + "원" : "34,600원";
    String orderDate =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    model.addAttribute("orderId", orderId);
    model.addAttribute("totalAmount", finalTotalAmount);
    model.addAttribute("orderDate", orderDate);

    // 현재 로그인한 사용자 정보 추가
    if (customUserDetails != null && customUserDetails.getUser() != null) {
      model.addAttribute("userName", customUserDetails.getUser().getName());
      model.addAttribute("userPhone", customUserDetails.getUser().getPhone());
      model.addAttribute("userEmail", customUserDetails.getUser().getEmail());
    } else {
      // 로그인하지 않은 경우 기본값
      model.addAttribute("userName", "홍길동");
      model.addAttribute("userPhone", "010-1234-5678");
      model.addAttribute("userEmail", "hong@example.com");
    }

    // 장바구니에서 전달받은 상품 정보 처리
    if (itemsJson != null && !itemsJson.isEmpty()) {
      try {
        // 이중 인코딩된 경우를 대비해 두 번 디코딩
        String decodedItems;
        try {
          decodedItems = java.net.URLDecoder.decode(itemsJson, StandardCharsets.UTF_8);

          // 한 번 더 디코딩 시도 (이중 인코딩된 경우)
          if (decodedItems.contains("%")) {
            decodedItems = java.net.URLDecoder.decode(decodedItems, StandardCharsets.UTF_8);
          }
        } catch (Exception e) {
          // 디코딩 실패 시 원본 사용
          decodedItems = itemsJson;
        }

        // JSON 파싱해서 실제 상품 총 가격 계산
        int attributeValue = deliveryFee != null ? Integer.parseInt(deliveryFee) : 3000;
        int attributeValue1 = discountAmount != null ? Integer.parseInt(discountAmount) : 0;
        try {
          // 간단한 JSON 파싱으로 변경
          String jsonStr = decodedItems.trim();
          if (jsonStr.startsWith("[") && jsonStr.endsWith("]")) {
            // "totalPrice" 값을 추출하는 간단한 방법
            int totalProductPrice = 0;
            String[] parts = jsonStr.split("\"totalPrice\":");
            for (int i = 1; i < parts.length; i++) {
              String part = parts[i];
              int commaIndex = part.indexOf(",");
              int bracketIndex = part.indexOf("}");
              int endIndex =
                  commaIndex > 0 && (bracketIndex < 0 || commaIndex < bracketIndex)
                      ? commaIndex
                      : bracketIndex;
              if (endIndex > 0) {
                String priceStr = part.substring(0, endIndex).trim();
                // 숫자가 아닌 문자 제거 (원, 콤마 등)
                priceStr = priceStr.replaceAll("[^0-9]", "");
                try {
                  if (!priceStr.isEmpty()) {
                    totalProductPrice += Integer.parseInt(priceStr);
                  }
                } catch (NumberFormatException nfe) {
                  // 숫자 변환 실패 시 무시
                }
              }
            }

            model.addAttribute("cartItems", decodedItems);
            model.addAttribute("itemCount", itemCount != null ? itemCount : "1");
            model.addAttribute("deliveryFee", attributeValue);
            model.addAttribute("discountAmount", attributeValue1);
            model.addAttribute("totalProductPrice", totalProductPrice);

            // 실제 총 금액 (원 단위)
            int actualTotalAmount =
                totalAmount != null ? Integer.parseInt(totalAmount) : totalProductPrice + 3000;
            model.addAttribute("actualTotalAmount", actualTotalAmount);
            model.addAttribute("totalAmount", actualTotalAmount);
          } else {
            throw new Exception("Invalid JSON format");
          }

        } catch (Exception e) {
          // JSON 파싱 실패 시 기본값 사용
          model.addAttribute("cartItems", decodedItems);
          model.addAttribute("itemCount", itemCount != null ? itemCount : "1");
          model.addAttribute("deliveryFee", attributeValue);
          model.addAttribute("discountAmount", attributeValue1);
          model.addAttribute("totalProductPrice", 10000);
          model.addAttribute("actualTotalAmount", 13000);
          model.addAttribute("totalAmount", 13000);
        }

      } catch (Exception e) {
        // JSON 파싱 실패 시 기본값 사용
        model.addAttribute("cartItems", "[]");
        model.addAttribute("itemCount", "1");
        model.addAttribute("deliveryFee", 3000);
        model.addAttribute("discountAmount", 0);
        model.addAttribute("actualTotalAmount", 34600);
        model.addAttribute("totalAmount", 34600);
      }
    } else {
      // 파라미터가 없을 경우 기본값
      model.addAttribute("cartItems", "[]");
      model.addAttribute("itemCount", "1");
      model.addAttribute("deliveryFee", 3000);
      model.addAttribute("discountAmount", 0);
      model.addAttribute("actualTotalAmount", 34600);
      model.addAttribute("totalAmount", 34600);
    }

    return "order/checkout";
  }

  /** 결제 성공 페이지 */
  @GetMapping("/success")
  public String success(
      @RequestParam(value = "orderId", required = false) String orderId,
      @RequestParam(value = "paymentKey", required = false) String paymentKey,
      @RequestParam(value = "amount", required = false) String amount,
      @RequestParam(value = "items", required = false) String itemsJson,
      @RequestParam(value = "deliveryFee", required = false) String deliveryFee,
      @RequestParam(value = "discountAmount", required = false) String discountAmount,
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      Model model) {

    try {
      // 주문 번호 생성 (없으면 현재 시간 기반으로 생성)
      String finalOrderId = orderId != null ? orderId : "ORD-" + System.currentTimeMillis();

      // 주문일시
      String orderDate =
          LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

      // 결제금액 계산 (중복된 값 처리)
      int totalAmount = 0;
      if (amount != null) {
        // 쉼표로 구분된 값이 있을 경우 첫 번째 값만 사용
        String cleanAmount = amount.split(",")[0].trim();
        try {
          totalAmount = Integer.parseInt(cleanAmount);
        } catch (NumberFormatException e) {
          System.out.println("=== amount 파싱 오류 ===");
          System.out.println("원본 amount: " + amount);
          System.out.println("정리된 amount: " + cleanAmount);
        }
      }

      // 배송 예정일 계산 (주문일 + 1-2일)
      LocalDateTime orderDateTime = LocalDateTime.now();
      LocalDateTime deliveryStart = orderDateTime.plusDays(1);
      LocalDateTime deliveryEnd = orderDateTime.plusDays(2);
      String deliveryDate =
          deliveryStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
              + " ~ "
              + deliveryEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

      // 주문 정보를 DB에 저장
      Order order = new Order();
      order.setOrderNumber(finalOrderId);
      order.setTotalPrice(totalAmount);
      order.setDeliveryFee(deliveryFee != null ? Integer.parseInt(deliveryFee) : 0);
      order.setDiscountAmount(discountAmount != null ? Integer.parseInt(discountAmount) : 0);
      order.setPaymentMethod("TOSS");
      order.setPaymentStatus(Order.PaymentStatus.PAID);
      order.setOrderStatus(Order.OrderStatus.ORDERED);
      // 현재 로그인된 사용자 정보 설정
      if (customUserDetails != null && customUserDetails.getUser() != null) {
        order.setUser(customUserDetails.getUser());

        // 실제 사용자 정보로 설정
        order.setTel(
            customUserDetails.getUser().getPhone() != null
                ? customUserDetails.getUser().getPhone()
                : "010-1234-5678");
        order.setReceiver(
            customUserDetails.getUser().getName() != null
                ? customUserDetails.getUser().getName()
                : "홍길동");
        order.setEmail(
            customUserDetails.getUser().getEmail() != null
                ? customUserDetails.getUser().getEmail()
                : "hong@example.com");
      } else {
        // 로그인 정보가 없을 경우 기본값
        order.setTel("010-1234-5678");
        order.setReceiver("홍길동");
        order.setEmail("hong@example.com");
      }

      // 하드코딩된 배송 정보 (나중에 배송지 선택 기능으로 변경 예정)
      order.setAddress("서울특별시 강남구 테헤란로 123"); // 기본 배송지
      order.setCard("TOSS"); // 결제 수단
      order.setZipCode("06123"); // 우편번호
      order.setOrderRequest(""); // 배송 요청사항
      order.setOrderPassword("1234"); // 배송 비밀번호

      // 주문 저장
      System.out.println("=== 주문 저장 시작 ===");
      System.out.println("Order Number: " + finalOrderId);
      System.out.println("Total Price: " + totalAmount);
      System.out.println(
          "User: "
              + (customUserDetails != null && customUserDetails.getUser() != null
                  ? customUserDetails.getUser().getUserId()
                  : "null"));

      Order savedOrder = orderService.createOrder(order);
      System.out.println("=== 주문 저장 완료 ===");
      System.out.println("Saved Order ID: " + savedOrder.getOrderId());

      // 상품 정보 처리 및 저장
      if (itemsJson != null && !itemsJson.isEmpty()) {
        try {
          // URL 디코딩
          String decodedItems = java.net.URLDecoder.decode(itemsJson, StandardCharsets.UTF_8);

          // 디버깅용 로그
          System.out.println("Original itemsJson: " + itemsJson);
          System.out.println("Decoded items: " + decodedItems);

          // JSON 파싱
          List<CartItem> cartItems = objectMapper.readValue(decodedItems, new TypeReference<>() {});

          // 주문 상품 저장
          System.out.println("=== 주문 상품 저장 시작 ===");
          for (CartItem cartItem : cartItems) {
            System.out.println("상품명: " + cartItem.getName());
            System.out.println("수량: " + cartItem.getQuantity());
            System.out.println("가격: " + cartItem.getPricePerItem());

            OrderItem orderItem = new OrderItem();
            orderItem.setProductName(cartItem.getName());
            orderItem.setProductImage(cartItem.getImage());
            orderItem.setOrderQuantity(cartItem.getQuantity());
            orderItem.setPricePerItem(cartItem.getPricePerItem());
            orderItem.setTotalPrice(cartItem.getTotalPrice());
            orderItem.setOrder(savedOrder);
            // perfume 관계는 null로 설정 (선택적 관계)
            orderItem.setPerfume(null);

            OrderItem savedOrderItem = orderService.addOrderItem(orderItem);
            System.out.println("저장된 OrderItem ID: " + savedOrderItem.getOrderItemId());
          }
          System.out.println("=== 주문 상품 저장 완료 ===");

          // 장바구니에서 구매한 상품들 삭제
          String userId =
              customUserDetails != null && customUserDetails.getUser() != null
                  ? customUserDetails.getUser().getUserId().toString()
                  : "user1"; // 임시 사용자 ID
          cartService.removePurchasedItems(userId, cartItems);

          // 위시리스트에서 구매한 상품들 삭제
          if (customUserDetails != null && customUserDetails.getUser() != null) {
            String loginId = customUserDetails.getUser().getLoginId();
            for (CartItem cartItem : cartItems) {
              try {
                if (cartItem.getId() != null) {
                  wishListService.wishperfumeDelete(cartItem.getId(), loginId);
                  System.out.println(
                      "위시리스트에서 상품 삭제 완료: "
                          + cartItem.getName()
                          + " (ID: "
                          + cartItem.getId()
                          + ")");
                }
              } catch (Exception e) {
                System.out.println("위시리스트 삭제 중 오류: " + e.getMessage());
              }
            }
          }

          model.addAttribute("cartItems", decodedItems);
        } catch (Exception e) {
          model.addAttribute("cartItems", "[]");
        }
      } else {
        model.addAttribute("cartItems", "[]");
      }

      // Model에 데이터 추가
      model.addAttribute("orderId", finalOrderId);
      model.addAttribute("orderDate", orderDate);
      model.addAttribute("totalAmount", String.format("%,d원", totalAmount));
      model.addAttribute("paymentKey", paymentKey);
      model.addAttribute("deliveryDate", deliveryDate);

      // 현재 로그인한 사용자 정보 추가
      if (customUserDetails != null && customUserDetails.getUser() != null) {
        model.addAttribute("userName", customUserDetails.getUser().getName());
        model.addAttribute("userPhone", customUserDetails.getUser().getPhone());
        model.addAttribute("userEmail", customUserDetails.getUser().getEmail());
      } else {
        // 로그인하지 않은 경우 기본값
        model.addAttribute("userName", "홍길동");
        model.addAttribute("userPhone", "010-1234-5678");
        model.addAttribute("userEmail", "hong@example.com");
      }

    } catch (Exception e) {
      // 오류 발생 시 상세 로그 출력
      System.out.println("=== 주문 저장 중 오류 발생 ===");
      System.out.println("오류 메시지: " + e.getMessage());

      // 오류 발생 시 기본값 설정
      model.addAttribute("orderId", "ORD-" + System.currentTimeMillis());
      model.addAttribute(
          "orderDate",
          LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
      model.addAttribute("totalAmount", "0원");
      model.addAttribute("paymentKey", paymentKey);
      model.addAttribute("deliveryDate", "2024-01-17 ~ 2024-01-18");
      model.addAttribute("cartItems", "[]");
    }

    return "order/success";
  }

  /** 결제 실패 페이지 */
  @GetMapping("/fail")
  public String fail(
      @RequestParam(value = "code", required = false) String errorCode,
      @RequestParam(value = "message", required = false) String errorMessage,
      @RequestParam(value = "orderId", required = false) String orderId,
      @RequestParam(value = "amount", required = false) String amount,
      Model model) {

    // 오류 발생 시간
    String errorTime =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    // 오류 코드와 메시지 처리
    String finalErrorCode = errorCode != null ? errorCode : "PAYMENT_FAILED";
    String finalErrorMessage = errorMessage != null ? errorMessage : "결제 처리 중 오류가 발생했습니다";

    // 오류 메시지 한글화
    String koreanErrorMessage = getKoreanErrorMessage(finalErrorCode, finalErrorMessage);

    // Model에 데이터 추가
    model.addAttribute("errorCode", finalErrorCode);
    model.addAttribute("errorMessage", koreanErrorMessage);
    model.addAttribute("errorTime", errorTime);
    model.addAttribute("orderId", orderId);
    model.addAttribute("amount", amount);

    return "order/fail";
  }

  /** 오류 코드를 한글 메시지로 변환 */
  private String getKoreanErrorMessage(String errorCode, String originalMessage) {
    return switch (errorCode) {
      case "CARD_DECLINED" -> "카드 결제가 거부되었습니다. 카드사에 문의해주세요.";
      case "INSUFFICIENT_FUNDS" -> "카드 잔액이 부족합니다. 다른 결제 수단을 이용해주세요.";
      case "INVALID_CARD" -> "유효하지 않은 카드입니다. 카드 정보를 확인해주세요.";
      case "EXPIRED_CARD" -> "만료된 카드입니다. 다른 카드를 이용해주세요.";
      case "CVC_MISMATCH" -> "CVC 번호가 일치하지 않습니다. 카드 뒷면의 CVC를 확인해주세요.";
      case "NETWORK_ERROR" -> "네트워크 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
      case "TIMEOUT" -> "결제 시간이 초과되었습니다. 다시 시도해주세요.";
      case "USER_CANCELED" -> "사용자가 결제를 취소했습니다.";
      default -> originalMessage;
    };
  }

  /** 결제 처리 (나중에 Toss API 연동) */
  @GetMapping("/process")
  public String processPayment(
      @RequestParam("orderId") String orderId, @RequestParam("amount") String amount) {
    // 여기서 실제 결제 처리 로직이 들어갈 예정
    // 현재는 테스트용으로 성공 페이지로 리다이렉트
    return "redirect:/order/success?orderId=" + orderId + "&amount=" + amount;
  }

  /** 테스트용 엔드포인트 */
  @GetMapping("/test")
  public String test() {
    System.out.println("=== OrderController test 메서드 실행 ===");
    return "order/success";
  }
}
