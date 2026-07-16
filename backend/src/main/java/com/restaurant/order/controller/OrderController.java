package com.restaurant.order.controller;

import java.util.List;

// import java.util.List;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.order.dto.CreateOrderRequest;
import com.restaurant.order.dto.OrderResponse;
import com.restaurant.order.dto.OrderSummaryResponse;
import com.restaurant.order.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    // 查單筆訂單詳情，含 items
    @GetMapping("/{orderId}")
    public OrderResponse getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId);
    }

    // 查某個會員的所有訂單不含 items

    @GetMapping("/user/{userId}")
    public List<OrderSummaryResponse> getOrdersByUserId(@PathVariable Long userId) {
        return orderService.getOrdersByUserId(userId);
    }
    // }

    // 查某個店家的所有訂單

    // @GetMapping("/store/{storeId}")
    // public List<OrderResponse> getOrdersByStore(
    // @PathVariable Long storeId) {

    // return orderService.getOrdersByStore(storeId);
    // }

}
