package com.restaurant.order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurant.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserUserId(Long userId);

    List<Order> findByStoreStoreId(Long storeId);

    // List<Order> findByStatus(String status);
}
