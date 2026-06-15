package com.restaurant.menu.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MenuEditDTO {

    private Long id;
    private Long categoryId;
    private String itemName;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private String allergenInfo;
    private Boolean isActive;
}