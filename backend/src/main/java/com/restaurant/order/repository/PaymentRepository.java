package com.restaurant.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurant.order.entity.Order;
import com.restaurant.order.entity.Payment;


public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findByOrder(Order order);
}
