package com.restaurant.menu.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity(name = "MenuCategory")
@Table(name = "menu_category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(name = "category_name", nullable = false)
    private String categoryName;

    @Column(name = "sort_order")
    private Integer sortOrder;

}
