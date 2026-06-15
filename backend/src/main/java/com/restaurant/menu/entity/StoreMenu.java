package com.restaurant.menu.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "store_menu") // 🎯 100% 對齊組長最新 DDL 表名
public class StoreMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_menu_id")
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "menu_item_id", nullable = false)
    private Long menuItemId;

    @Column(name = "price")
    private BigDecimal price; // 該門市售價（如果為 null 則自動退回總部的 base_price）

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true; // 該門市是否供應
}