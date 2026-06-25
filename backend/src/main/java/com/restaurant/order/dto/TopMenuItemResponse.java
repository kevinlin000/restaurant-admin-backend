package com.restaurant.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TopMenuItemResponse {

    private String itemName;
    private Long quantity;
}
