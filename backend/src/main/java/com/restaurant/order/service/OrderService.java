package com.restaurant.order.service;

import java.math.BigDecimal;

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
    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    private final PaymentRepository paymentRepository;

    private final PaymentService paymentService;

    OrderService(OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            PaymentRepository paymentRepository,
            PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;

    }

    public OrderResponse createOrder(CreateOrderRequest request) {

        // 1. 查詢 User

        // 2. 查詢 Store

        // 3. 查詢 Reservation

        // 4. 查詢 MenuItem

        // 5. 計算 totalAmount

        // 6. 計算 finalAmount

        // 7. 計算 pointsEarned

        // 8. 建立 Order
        Order order = Order.builder()
                .userId(request.getUserId())
                .storeId(request.getStoreId())
                .tableId(request.getTableId())
                .reservationId(request.getReservationId())
                .orderType(request.getOrderType())
                // 計算假資料
                .totalAmount(BigDecimal.valueOf(100))
                .finalAmount(BigDecimal.valueOf(100))
                .pointsEarned(0)
                .pointsUsed(request.getPointsUsed())
                .invoiceType(request.getInvoiceType())
                .carrierNumber(request.getCarrierNumber())
                .status("UNPAID")
                .build();
        // 9. 建立 OrderItem

        // 10. 回傳 OrderResponse
        Order savedOrder = orderRepository.save(order);
        
System.out.println("finalAmount=" + savedOrder.getFinalAmount());
        // 2. 建立 Payment
        Payment payment = Payment.builder()
        .order(savedOrder)
        .paymentMethod(request.getPaymentMethod())
        .paymentStatus("UNPAID")
        .amount(savedOrder.getFinalAmount())
        .build();

        paymentRepository.save(payment);
        // paymentService.createUnpaidPayment(savedOrder, request.getPaymentMethod());
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
                .userId(order.getUserId())
                .storeId(order.getStoreId())
                .tableId(order.getTableId())
                .reservationId(order.getReservationId())
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
