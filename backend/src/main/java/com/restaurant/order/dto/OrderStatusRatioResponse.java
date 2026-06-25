package com.restaurant.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderStatusRatioResponse {

    private String status;
    private Long count;
}