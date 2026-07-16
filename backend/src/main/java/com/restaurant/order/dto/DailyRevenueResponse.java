package com.restaurant.order.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DailyRevenueResponse {

    private LocalDate date;
    private BigDecimal revenue;
}
