package com.restaurant.order.dto;


import java.util.List;

import lombok.Data;

@Data
public class CreateOrderRequest {
     private Long userId;

    private Long storeId;

    private Long tableId;

    private Long reservationId;

    private String orderType;

    // private BigDecimal totalAmount;

    // private BigDecimal finalAmount;

    private Integer pointsUsed;

    // private Integer pointsEarned;

    private List<OrderItemRequest> items;
   
}
