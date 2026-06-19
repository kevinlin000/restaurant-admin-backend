package com.restaurant.order.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateOrderRequest {

    @NotNull(message = "會員 ID 不可為空")
    private Long userId;

    @NotNull(message = "門市 ID 不可為空")
    private Long storeId;

    private Long tableId;

    private Long reservationId;

    @Pattern(regexp = "DINE_IN|TAKEOUT|TAKE_OUT", message = "訂單類型只支援 DINE_IN 或 TAKEOUT")
    private String orderType;

    // private BigDecimal totalAmount;

    // private BigDecimal finalAmount;

    @Min(value = 0, message = "折抵點數不可小於 0")
    private Integer pointsUsed;

    // private Integer pointsEarned;
    
    private String invoiceType;

    private String carrierNumber;

    private String paymentMethod;

    @Valid
    @NotEmpty(message = "訂單至少需要一個餐點")
    private List<OrderItemRequest> items;
   
}
