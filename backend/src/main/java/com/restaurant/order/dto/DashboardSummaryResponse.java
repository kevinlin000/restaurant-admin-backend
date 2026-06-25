package com.restaurant.order.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardSummaryResponse {

    private BigDecimal todayRevenue;
    private BigDecimal monthRevenue;
    private Long todayOrders;
    private BigDecimal averageOrderAmount;
}
