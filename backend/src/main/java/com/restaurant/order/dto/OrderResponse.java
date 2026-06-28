package com.restaurant.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponse {

    private Long orderId;

    private Long userId;

    private Long storeId;

    private Long tableId;

    private Long reservationId;

    private String orderType;

    private BigDecimal totalAmount;

    private BigDecimal finalAmount;

    private Integer pointsUsed;

    private Integer pointsEarned;

    private String status;

    private LocalDateTime createdAt;

    private String paymentMethod;

    private String paymentStatus;

    private String invoiceType;

    private String carrierNumber;
    
    private String tableNumber;

    private List<OrderItemResponse> items;

}
