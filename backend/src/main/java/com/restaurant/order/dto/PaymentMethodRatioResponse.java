package com.restaurant.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentMethodRatioResponse {

    private String paymentMethod;
    private Long count;
}
