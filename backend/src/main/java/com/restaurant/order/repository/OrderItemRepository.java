package com.restaurant.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;


import com.restaurant.order.entity.OrderItem;


public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
