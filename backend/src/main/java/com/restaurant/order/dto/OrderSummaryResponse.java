package com.restaurant.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class OrderSummaryResponse {
     private Long orderId;

    private Long userId;

    private Long storeId;

    private String orderType;

    private BigDecimal totalAmount;

    private BigDecimal finalAmount;

    private String paymentMethod;

    private String paymentStatus;

    private String invoiceType;

    private String carrierNumber;

    private String status;

    private String tableNumber;

    private LocalDateTime createdAt;
}
