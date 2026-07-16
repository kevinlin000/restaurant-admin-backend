package com.restaurant.order.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.restaurant.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
  List<Order> findByUserUserId(Long userId);

  List<Order> findByStoreStoreId(Long storeId);

  List<Order> findAllByOrderByCreatedAtDesc();

  @Query("""
          SELECT COALESCE(SUM(o.finalAmount), 0)
          FROM Order o
          WHERE o.createdAt >= :start
            AND o.createdAt < :end
            AND o.status = 'COMPLETED'
      """)
  BigDecimal sumRevenueBetween(LocalDateTime start, LocalDateTime end);

  @Query("""
          SELECT COUNT(o)
          FROM Order o
          WHERE o.createdAt >= :start
            AND o.createdAt < :end
      """)
  Long countOrdersBetween(LocalDateTime start, LocalDateTime end);

  @Query("""
          SELECT COUNT(o)
          FROM Order o
          WHERE o.createdAt >= :start
            AND o.createdAt < :end
            AND o.status = 'COMPLETED'
      """)
  Long countCompletedOrdersBetween(LocalDateTime start, LocalDateTime end);

  @Query("""
          SELECT COALESCE(AVG(o.finalAmount), 0)
          FROM Order o
          WHERE o.createdAt >= :start
            AND o.createdAt < :end
            AND o.status = 'COMPLETED'
      """)
  BigDecimal averageOrderAmountBetween(LocalDateTime start, LocalDateTime end);

  @Query(value = """
          SELECT DATE(o.created_at) AS order_date,
                 COALESCE(SUM(o.final_amount), 0) AS revenue
          FROM orders o
          WHERE o.created_at >= :start
            AND o.created_at < :end
            AND o.status = 'COMPLETED'
          GROUP BY DATE(o.created_at)
          ORDER BY DATE(o.created_at)
      """, nativeQuery = true)
  List<Object[]> findDailyRevenueRaw(LocalDateTime start, LocalDateTime end);

  @Query(value = """
          SELECT o.status AS status,
                 COUNT(*) AS count
          FROM orders o
          WHERE o.created_at >= :start
            AND o.created_at < :end
          GROUP BY o.status
      """, nativeQuery = true)
  List<Object[]> findOrderStatusRatioRaw(LocalDateTime start, LocalDateTime end);

  // List<Order> findByStatus(String status);
}
