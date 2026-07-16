package com.restaurant.reservation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "slot_id", nullable = false)
    private Long slotId;

    @Column(name = "party_size", nullable = false)
    private Integer partySize;

    // 訂位狀態流程：PENDING -> RESERVED -> ASSIGNED -> CHECKED_IN，取消則為 CANCELLED。
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "deposit_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal depositAmount = BigDecimal.ZERO;

    @Column(name = "payment_status", length = 20)
    @Builder.Default
    private String paymentStatus = "UNPAID";

    @Column(name = "special_request", columnDefinition = "TEXT")
    private String specialRequest;

    @Column(name = "customer_name", nullable = false, length = 50)
    private String customerName;

    @Column(name = "customer_phone", nullable = false, length = 20)
    private String customerPhone;

    @Column(name = "customer_email", length = 100)
    private String customerEmail;

    @Column(name = "access_token", length = 64, unique = true)
    private String accessToken;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 已入座 -> 不能再編輯、取消或更改桌位(流程結束)
    public boolean isLocked() {
        return "CHECKED_IN".equals(status) || "COMPLETED".equals(status);
    }
}
