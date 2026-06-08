package com.restaurant.order.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.restaurant.order.dto.CreateOrderRequest;
import com.restaurant.order.dto.OrderResponse;
import com.restaurant.order.entity.Order;
import com.restaurant.order.repository.OrderItemRepository;
import com.restaurant.order.repository.OrderRepository;
import com.restaurant.order.repository.PaymentRepository;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    public OrderResponse createOrder(CreateOrderRequest request) {

        // 1. 查詢 User

        // 2. 查詢 Store

        // 3. 查詢 Reservation

        // 4. 查詢 MenuItem

        // 5. 計算 totalAmount

        // 6. 計算 finalAmount

        // 7. 計算 pointsEarned

        // 8. 建立 Order

        // 9. 建立 OrderItem

        // 10. 回傳 OrderResponse
        return null;
    }

    public OrderResponse getOrderById(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("找不到訂單"));

        return convertToResponse(order);
    }

    private OrderResponse convertToResponse(Order order) {

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                // .userId(order.getUser() != null ? order.getUser().getUserId() : null)
                // .storeId(order.getStore() != null ? order.getStore().getStoreId() : null)
                // .tableId(order.getTableInfo() != null ? order.getTableInfo().getTableId() :
                // null)
                // .reservationId(order.getReservation() != null ?
                // order.getReservation().getReservationId() : null)
                .orderType(order.getOrderType())
                .totalAmount(order.getTotalAmount())
                .finalAmount(order.getFinalAmount())
                .pointsUsed(order.getPointsUsed())
                .pointsEarned(order.getPointsEarned())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }

}
