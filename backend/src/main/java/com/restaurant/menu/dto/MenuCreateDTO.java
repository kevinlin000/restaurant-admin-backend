package com.restaurant.menu.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MenuCreateDTO {
    private Long categoryId;
    private String itemName;
    private String description;
    private BigDecimal price; // 對齊 base_price
    private String imageUrl;
    private String allergenInfo;
    // 🎯 修正：不要再用 String status，全面換成最新官方布林值！
    private Boolean isActive = true;
}