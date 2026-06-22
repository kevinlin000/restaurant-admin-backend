package com.restaurant.order.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.restaurant.common.BusinessException;
import com.restaurant.menu.entity.StoreMenu;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.member.entity.User;
import com.restaurant.member.repository.UserRepository;
import com.restaurant.menu.entity.MenuItem;
import com.restaurant.menu.repository.MenuItemRepository;
import com.restaurant.menu.repository.StoreMenuRepository;
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
        private static final String ORDER_TYPE_DINE_IN = "DINE_IN";
        private static final String ORDER_TYPE_TAKEOUT = "TAKEOUT";

        private final OrderRepository orderRepository;

        private final OrderItemRepository orderItemRepository;

        private final PaymentRepository paymentRepository;

        private final UserRepository userRepository;

        private final StoreRepository storeRepository;

        private final TableInfoRepository tableInfoRepository;

        private final MenuItemRepository menuItemRepository;

        private final StoreMenuRepository storeMenuRepository;

        OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        PaymentRepository paymentRepository,
                        UserRepository userRepository,
                        StoreRepository storeRepository,
                        TableInfoRepository tableInfoRepository,
                        MenuItemRepository menuItemRepository,
                        StoreMenuRepository storeMenuRepository) {
                this.orderRepository = orderRepository;
                this.orderItemRepository = orderItemRepository;
                this.paymentRepository = paymentRepository;
                this.userRepository = userRepository;
                this.storeRepository = storeRepository;
                this.tableInfoRepository = tableInfoRepository;
                this.menuItemRepository = menuItemRepository;
                this.storeMenuRepository = storeMenuRepository;

        }

        @Transactional
        public OrderResponse createOrder(CreateOrderRequest request) {
                String orderType = normalizeOrderType(request.getOrderType());

                // 1. 查詢 User
                User user = userRepository.findById(request.getUserId())
                                .orElseThrow(() -> new BusinessException("找不到會員"));

                // 2. 查詢 Store
                Store store = storeRepository.findByStoreIdAndIsDeletedFalse(request.getStoreId())
                                .orElseThrow(() -> new BusinessException("找不到門市"));
                if (!"OPEN".equals(store.getStatus())) {
                        throw new BusinessException("門市目前未開放點餐");
                }

                // 3. 查詢 Table
                TableInfo table = resolveTable(request, orderType);

                // 4. 查詢 Reservation

                // 5. 查詢 MenuItem// 6. 計算 totalAmount
                List<OrderLine> orderLines = buildOrderLines(request.getStoreId(), request.getItems());
                BigDecimal totalAmount = orderLines.stream()
                                .map(OrderLine::subtotal)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

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
                                .orderType(orderType)
                                // 計算假資料
                                .totalAmount(totalAmount)
                                .finalAmount(finalAmount)
                                .pointsEarned(0)
                                .pointsUsed(request.getPointsUsed() != null ? request.getPointsUsed() : 0)
                                .invoiceType(request.getInvoiceType())
                                .carrierNumber(request.getCarrierNumber())
                                .status("PENDING")
                                .build();

                // 11. 儲存 Order
                Order savedOrder = orderRepository.save(order);

                // System.out.println("finalAmount=" + savedOrder.getFinalAmount());
                for (OrderLine orderLine : orderLines) {
                        // 10. 建立 OrderItem
                        OrderItem orderItem = OrderItem.builder()
                                        .order(savedOrder)
                                        .menuItem(orderLine.menuItem())
                                        .quantity(orderLine.quantity())
                                        .unitPrice(orderLine.unitPrice())
                                        .subtotal(orderLine.subtotal())
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
                                .orElseThrow(() -> new BusinessException("找不到訂單"));

                return convertToResponse(order);
        }

        public List<OrderResponse> getAllOrdersForAdmin() {
                return orderRepository.findAll()
                                .stream()
                                .map(this::convertToResponse)
                                .toList();
        }

        public OrderResponse updateOrderStatusForAdmin(Long orderId, String status) {
                if (status == null || status.isBlank()) {
                        throw new BusinessException("訂單狀態不可為空");
                }

                String newStatus = status.trim().toUpperCase();

                if (!newStatus.equals("PENDING")
                                && !newStatus.equals("CONFIRMED")
                                && !newStatus.equals("PREPARING")
                                && !newStatus.equals("READY")
                                && !newStatus.equals("COMPLETED")
                                && !newStatus.equals("CANCELLED")) {
                        throw new BusinessException("不支援的訂單狀態");
                }

                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new BusinessException("找不到訂單"));

                Payment payment = paymentRepository.findByOrder(order);

                String currentStatus = order.getStatus();

                if ("COMPLETED".equals(currentStatus) || "CANCELLED".equals(currentStatus)) {
                        throw new BusinessException("已完成或已取消的訂單不可再修改");
                }

                if ("COMPLETED".equals(newStatus)) {
                        if (payment == null || !"PAID".equals(payment.getPaymentStatus())) {
                                throw new BusinessException("訂單尚未付款，不能設為已完成");
                        }
                }

                // 狀態流程防呆
                if (!isValidStatusTransition(currentStatus, newStatus)) {
                        throw new BusinessException("訂單狀態必須依流程更新");
                }

                order.setStatus(newStatus);

                Order savedOrder = orderRepository.save(order);

                return convertToResponse(savedOrder);
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
                                .tableId(order.getTable() != null ? order.getTable().getTableId() : null)
                                .reservationId(order.getReservationId())
                                .orderType(order.getOrderType())
                                .totalAmount(order.getTotalAmount())
                                .finalAmount(order.getFinalAmount())
                                .pointsUsed(order.getPointsUsed())
                                .pointsEarned(order.getPointsEarned())
                                .paymentMethod(payment != null ? payment.getPaymentMethod() : null)
                                .paymentStatus(payment != null ? payment.getPaymentStatus() : null)
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
                                .paymentStatus(payment != null ? payment.getPaymentStatus() : null)
                                .invoiceType(order.getInvoiceType())
                                .carrierNumber(order.getCarrierNumber())
                                .status(order.getStatus())
                                .createdAt(order.getCreatedAt())
                                .build();
        }

        private TableInfo resolveTable(CreateOrderRequest request, String orderType) {
                if (request.getTableId() == null) {
                        if (ORDER_TYPE_DINE_IN.equals(orderType)) {
                                throw new BusinessException("內用訂單必須指定桌位");
                        }
                        return null;
                }

                return tableInfoRepository.findByTableIdAndStoreId(request.getTableId(), request.getStoreId())
                                .orElseThrow(() -> new BusinessException("桌位不屬於指定門市"));
        }

        private List<OrderLine> buildOrderLines(Long storeId, List<OrderItemRequest> items) {
                if (items == null || items.isEmpty()) {
                        throw new BusinessException("訂單至少需要一個餐點");
                }

                List<OrderLine> orderLines = new ArrayList<>();

                for (OrderItemRequest itemRequest : items) {
                        if (itemRequest.getMenuItemId() == null) {
                                throw new BusinessException("餐點 ID 不可為空");
                        }
                        if (itemRequest.getQuantity() == null || itemRequest.getQuantity() < 1) {
                                throw new BusinessException("餐點數量至少為 1");
                        }

                        StoreMenu storeMenu = storeMenuRepository
                                        .findByStoreIdAndMenuItemIdAndIsAvailableTrue(storeId,
                                                        itemRequest.getMenuItemId())
                                        .orElseThrow(() -> new BusinessException("此門市未供應部分餐點"));
                        MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                                        .orElseThrow(() -> new BusinessException("找不到餐點"));
                        if (!Boolean.TRUE.equals(menuItem.getIsActive())) {
                                throw new BusinessException("餐點已下架");
                        }

                        BigDecimal unitPrice = storeMenu.getPrice() != null ? storeMenu.getPrice()
                                        : menuItem.getPrice();
                        if (unitPrice == null) {
                                throw new BusinessException("餐點價格未設定");
                        }

                        Integer quantity = itemRequest.getQuantity();
                        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
                        orderLines.add(new OrderLine(menuItem, quantity, unitPrice, subtotal));
                }

                return orderLines;
        }

        private String normalizeOrderType(String orderType) {
                if (orderType == null || orderType.isBlank()) {
                        return ORDER_TYPE_DINE_IN;
                }

                String normalized = orderType.trim().toUpperCase();
                if ("TAKE_OUT".equals(normalized)) {
                        return ORDER_TYPE_TAKEOUT;
                }
                if (ORDER_TYPE_DINE_IN.equals(normalized) || ORDER_TYPE_TAKEOUT.equals(normalized)) {
                        return normalized;
                }

                throw new BusinessException("不支援的訂單類型");
        }

        private record OrderLine(MenuItem menuItem, Integer quantity, BigDecimal unitPrice, BigDecimal subtotal) {
        }

        private boolean isValidStatusTransition(String currentStatus, String newStatus) {

                if (currentStatus.equals(newStatus)) {
                        return true;
                }

                if ("CANCELLED".equals(newStatus)) {
                        return true;
                }

                if ("PENDING".equals(currentStatus)) {
                        return "CONFIRMED".equals(newStatus);
                }

                if ("CONFIRMED".equals(currentStatus)) {
                        return "PREPARING".equals(newStatus);
                }

                if ("PREPARING".equals(currentStatus)) {
                        return "READY".equals(newStatus);
                }

                if ("READY".equals(currentStatus)) {
                        return "COMPLETED".equals(newStatus);
                }

                return false;
        }

}
