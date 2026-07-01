package com.restaurant.order.service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.restaurant.order.dto.DailyRevenueResponse;
import com.restaurant.order.dto.DashboardSummaryResponse;
import com.restaurant.order.dto.OrderStatusRatioResponse;
import com.restaurant.order.dto.PaymentMethodRatioResponse;
import com.restaurant.order.dto.TopMenuItemResponse;
import com.restaurant.order.repository.OrderItemRepository;
import com.restaurant.order.repository.OrderRepository;
import com.restaurant.order.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;

    public DashboardSummaryResponse getSummary() {
        LocalDate today = LocalDate.now();

        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();

        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime nextMonthStart = today.plusMonths(1).withDayOfMonth(1).atStartOfDay();

       return new DashboardSummaryResponse(
        orderRepository.sumRevenueBetween(todayStart, tomorrowStart),
        orderRepository.sumRevenueBetween(monthStart, nextMonthStart),
        orderRepository.countCompletedOrdersBetween(todayStart, tomorrowStart),
        orderRepository.averageOrderAmountBetween(monthStart, nextMonthStart)
);
    }

    public List<DailyRevenueResponse> getDailyRevenue() {
        LocalDate today = LocalDate.now();

        LocalDateTime start = today.minusDays(6).atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        return orderRepository.findDailyRevenueRaw(start, end)
                .stream()
                .map(row -> new DailyRevenueResponse(
                        ((Date) row[0]).toLocalDate(),
                        (BigDecimal) row[1]
                ))
                .toList();
    }

    public List<TopMenuItemResponse> getTopMenuItems() {
        LocalDate today = LocalDate.now();

        LocalDateTime start = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = today.plusMonths(1).withDayOfMonth(1).atStartOfDay();

        return orderItemRepository.findTopMenuItemsRaw(start, end)
                .stream()
                .map(row -> new TopMenuItemResponse(
                        (String) row[0],
                        ((Number) row[1]).longValue()
                ))
                .toList();
    }

    public List<PaymentMethodRatioResponse> getPaymentMethodRatio() {
        LocalDate today = LocalDate.now();

        LocalDateTime start = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = today.plusMonths(1).withDayOfMonth(1).atStartOfDay();

        return paymentRepository.findPaymentMethodRatioRaw(start, end)
                .stream()
                .map(row -> new PaymentMethodRatioResponse(
                        (String) row[0],
                        ((Number) row[1]).longValue()
                ))
                .toList();
    }

    public List<OrderStatusRatioResponse> getOrderStatusRatio() {
        LocalDate today = LocalDate.now();

        LocalDateTime start = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = today.plusMonths(1).withDayOfMonth(1).atStartOfDay();

        return orderRepository.findOrderStatusRatioRaw(start, end)
                .stream()
                .map(row -> new OrderStatusRatioResponse(
                        (String) row[0],
                        ((Number) row[1]).longValue()
                ))
                .toList();
    }
}