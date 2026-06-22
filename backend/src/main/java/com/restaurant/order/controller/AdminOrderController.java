package com.restaurant.order.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.restaurant.order.dto.OrderResponse;
import com.restaurant.order.service.OrderService;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 後台：查詢全部訂單
    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrdersForAdmin();
    }

    // 後台：查詢單筆訂單明細
    @GetMapping("/{orderId}")
    public OrderResponse getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId);
    }

    // 後台：更新訂單狀態
    @PatchMapping("/{orderId}/status")
    public OrderResponse updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request
    ) {
        String status = request.get("status");
        return orderService.updateOrderStatusForAdmin(orderId, status);
    }
}
