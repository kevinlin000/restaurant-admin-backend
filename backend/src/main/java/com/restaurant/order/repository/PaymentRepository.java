package com.restaurant.order.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.restaurant.order.entity.Order;
import com.restaurant.order.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findByOrder(Order order);

    @Query(value = """
        SELECT p.payment_method AS payment_method,
               COUNT(*) AS count
        FROM payment p
        JOIN orders o ON p.order_id = o.order_id
        WHERE o.created_at >= :start
          AND o.created_at < :end
        GROUP BY p.payment_method
    """, nativeQuery = true)
    List<Object[]> findPaymentMethodRatioRaw(LocalDateTime start, LocalDateTime end);
}
