package com.restaurant.order.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequest {
    @NotNull(message = "餐點 ID 不可為空")
    private Long menuItemId;

    @NotNull(message = "餐點數量不可為空")
    @Min(value = 1, message = "餐點數量至少為 1")
    private Integer quantity;

    
}
