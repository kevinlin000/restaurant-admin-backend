package com.restaurant.order.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.restaurant.order.dto.CreateOrderRequest;
import com.restaurant.order.dto.OrderResponse;
import com.restaurant.order.entity.Order;
import com.restaurant.order.entity.Payment;
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
        Order order = new Order();
        // 9. 建立 OrderItem

        // 10. 回傳 OrderResponse
        Order savedOrder = orderRepository.save(order);

        // 2. 建立 Payment
        Payment payment = Payment.builder()
                .order(savedOrder)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus("UNPAID")
                .build();

        paymentRepository.save(payment);

        // 3. 回傳
        return

        convertToResponse(savedOrder);
    }

    public OrderResponse getOrderById(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("找不到訂單"));

        return convertToResponse(order);
    }

    private OrderResponse convertToResponse(Order order) {

        Payment payment = paymentRepository.findByOrder(order);
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
                .paymentMethod(payment != null ? payment.getPaymentMethod() : null)
                .invoiceType(order.getInvoiceType())
                .carrierNumber(order.getCarrierNumber())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }

}
