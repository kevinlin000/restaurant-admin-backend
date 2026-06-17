package com.restaurant.order.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.restaurant.member.entity.User;
import com.restaurant.member.repository.UserRepository;
import com.restaurant.menu.entity.MenuItem;
import com.restaurant.menu.repository.MenuItemRepository;
import com.restaurant.order.dto.CreateOrderRequest;
import com.restaurant.order.dto.OrderItemRequest;
import com.restaurant.order.dto.OrderItemResponse;
import com.restaurant.order.dto.OrderResponse;
import com.restaurant.order.dto.OrderSummaryResponse;
import com.restaurant.order.entity.Order;
import com.restaurant.order.entity.OrderItem;
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

        private final MenuItemRepository menuItemRepository;

        OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        PaymentRepository paymentRepository,
                        PaymentService paymentService,
                        UserRepository userRepository,
                        StoreRepository storeRepository,
                        TableInfoRepository tableInfoRepository,
                        MenuItemRepository menuItemRepository) {
                this.orderRepository = orderRepository;
                this.orderItemRepository = orderItemRepository;
                this.paymentRepository = paymentRepository;
                this.paymentService = paymentService;
                this.userRepository = userRepository;
                this.storeRepository = storeRepository;
                this.tableInfoRepository = tableInfoRepository;
                this.menuItemRepository = menuItemRepository;

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

                // 5. 查詢 MenuItem// 6. 計算 totalAmount
                BigDecimal totalAmount = BigDecimal.ZERO;

                for (OrderItemRequest itemRequest : request.getItems()) {
                        MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                                        .orElseThrow(() -> new RuntimeException("找不到餐點"));

                        BigDecimal subtotal = menuItem.getPrice()
                                        .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

                        totalAmount = totalAmount.add(subtotal);
                }

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

                // 11. 儲存 Order
                Order savedOrder = orderRepository.save(order);

                // System.out.println("finalAmount=" + savedOrder.getFinalAmount());
                for (OrderItemRequest itemRequest : request.getItems()) {
                        MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                                        .orElseThrow(() -> new RuntimeException("找不到餐點"));

                        BigDecimal unitPrice = menuItem.getPrice();

                        BigDecimal subtotal = unitPrice.multiply(
                                        BigDecimal.valueOf(itemRequest.getQuantity()));

                        // 10. 建立 OrderItem
                        OrderItem orderItem = OrderItem.builder()
                                        .order(savedOrder)
                                        .menuItem(menuItem)
                                        .quantity(itemRequest.getQuantity())
                                        .unitPrice(unitPrice)
                                        .subtotal(subtotal)
                                        .build();

                        orderItemRepository.save(orderItem);
                }
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
                List<OrderItem> orderItems = orderItemRepository.findByOrder(order);

                List<OrderItemResponse> itemResponses = orderItems.stream()
                                .map(item -> OrderItemResponse.builder()
                                                .menuItemId(item.getMenuItem().getId())
                                                .itemName(item.getMenuItem().getItemName())
                                                .quantity(item.getQuantity())
                                                .unitPrice(item.getUnitPrice())
                                                .subtotal(item.getSubtotal())
                                                .build())
                                .collect(Collectors.toList());
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
                                .items(itemResponses)
                                .build();
        }

        public List<OrderSummaryResponse> getOrdersByUserId(Long userId) {

                List<Order> orders = orderRepository.findByUserUserId(userId);

                return orders.stream()
                                .map(this::convertToSummaryResponse)
                                .collect(Collectors.toList());
        }

        private OrderSummaryResponse convertToSummaryResponse(Order order) {

                Payment payment = paymentRepository.findByOrder(order);

                return OrderSummaryResponse.builder()
                                .orderId(order.getOrderId())
                                .userId(order.getUser().getUserId())
                                .storeId(order.getStore().getStoreId())
                                .orderType(order.getOrderType())
                                .totalAmount(order.getTotalAmount())
                                .finalAmount(order.getFinalAmount())
                                .paymentMethod(payment != null ? payment.getPaymentMethod() : null)
                                .invoiceType(order.getInvoiceType())
                                .carrierNumber(order.getCarrierNumber())
                                .status(order.getStatus())
                                .createdAt(order.getCreatedAt())
                                .build();
        }

}
