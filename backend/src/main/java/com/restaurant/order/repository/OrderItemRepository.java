package com.restaurant.order.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.restaurant.order.entity.Order;
import com.restaurant.order.entity.OrderItem;


public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);

     @Query(value = """
        SELECT mi.item_name AS item_name,
               COALESCE(SUM(oi.quantity), 0) AS quantity
        FROM order_item oi
        JOIN orders o ON oi.order_id = o.order_id
        JOIN menu_item mi ON oi.menu_item_id = mi.menu_item_id
        WHERE o.created_at >= :start
          AND o.created_at < :end
          AND o.status = 'COMPLETED'
        GROUP BY mi.item_name
        ORDER BY quantity DESC
        LIMIT 10
    """, nativeQuery = true)
    List<Object[]> findTopMenuItemsRaw(LocalDateTime start, LocalDateTime end);
}
