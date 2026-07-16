package com.restaurant.order.controller;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.order.dto.DailyRevenueResponse;
import com.restaurant.order.dto.DashboardSummaryResponse;
import com.restaurant.order.dto.OrderStatusRatioResponse;
import com.restaurant.order.dto.PaymentMethodRatioResponse;
import com.restaurant.order.dto.TopMenuItemResponse;
import com.restaurant.order.service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public DashboardSummaryResponse getSummary() {
        return dashboardService.getSummary();
    }

    @GetMapping("/daily-revenue")
    public List<DailyRevenueResponse> getDailyRevenue() {
        return dashboardService.getDailyRevenue();
    }

    @GetMapping("/top-menu-items")
    public List<TopMenuItemResponse> getTopMenuItems() {
        return dashboardService.getTopMenuItems();
    }

    @GetMapping("/payment-method-ratio")
    public List<PaymentMethodRatioResponse> getPaymentMethodRatio() {
        return dashboardService.getPaymentMethodRatio();
    }

    @GetMapping("/order-status-ratio")
    public List<OrderStatusRatioResponse> getOrderStatusRatio() {
        return dashboardService.getOrderStatusRatio();
    }
}