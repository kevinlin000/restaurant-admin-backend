package com.restaurant.order.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.restaurant.member.entity.User;
import com.restaurant.member.repository.UserRepository;
import com.restaurant.order.dto.CreateOrderRequest;
import com.restaurant.order.dto.OrderResponse;
import com.restaurant.order.entity.Order;
import com.restaurant.order.entity.Payment;
import com.restaurant.order.repository.OrderItemRepository;
import com.restaurant.order.repository.OrderRepository;
import com.restaurant.order.repository.PaymentRepository;
import com.restaurant.store.entity.Store;
import com.restaurant.store.entity.TableInfo;
import com.restaurant.store.repository.StoreRepository;
import com.restaurant.store.repository.TableInfoRepository;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    private final PaymentRepository paymentRepository;

    private final PaymentService paymentService;

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final TableInfoRepository tableInfoRepository;

    OrderService(OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            PaymentRepository paymentRepository,
            PaymentService paymentService,
            UserRepository userRepository,
            StoreRepository storeRepository,
            TableInfoRepository tableInfoRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
        this.tableInfoRepository = tableInfoRepository;

    }

    public OrderResponse createOrder(CreateOrderRequest request) {

        // 1. 查詢 User
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("找不到會員"));

        // 2. 查詢 Store
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new RuntimeException("找不到店家"));

        // 3. 查詢 Table
        TableInfo table = tableInfoRepository.findById(request.getTableId())
                .orElseThrow(() -> new RuntimeException("找不到桌位"));

        // 4. 查詢 Reservation

        // 5. 查詢 MenuItem

        // 6. 計算 totalAmount
        BigDecimal totalAmount = BigDecimal.valueOf(100);

        // 7. 計算 finalAmount
        BigDecimal pointsDiscount = BigDecimal.valueOf(
                request.getPointsUsed() != null ? request.getPointsUsed() : 0);

        BigDecimal finalAmount = totalAmount.subtract(pointsDiscount);

        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }
        // 8. pointsEarned 由會員模組計算，這邊先放 0

        // 9. 建立 Order
        Order order = Order.builder()
                .user(user)
                .store(store)
                .table(table)
                .reservationId(request.getReservationId())
                .orderType(request.getOrderType())
                // 計算假資料
                .totalAmount(totalAmount)
                .finalAmount(finalAmount)
                .pointsEarned(0)
                .pointsUsed(request.getPointsUsed() != null ? request.getPointsUsed() : 0)
                .invoiceType(request.getInvoiceType())
                .carrierNumber(request.getCarrierNumber())
                .status("UNPAID")
                .build();
        // 10. 建立 OrderItem

        // // 11. 儲存 Order
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
        return convertToResponse(savedOrder);
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
                .userId(order.getUser().getUserId())
                .storeId(order.getStore().getStoreId())
                .tableId(order.getTable().getTableId())
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
