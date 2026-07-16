package com.restaurant.order.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.restaurant.member.entity.User;
import com.restaurant.reservation.entity.Reservation;
import com.restaurant.store.entity.Store;
import com.restaurant.store.entity.TableInfo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    // 之後這四個column 要拿掉換關聯
    @Column(name = "order_id")
    private Long orderId;

    // @Column(name = "user_id")
    // private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // @Column(name = "store_id")
    // private Long storeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id")
    private TableInfo table;
    // @Column(name = "table_id")
    // private Long tableId;

    // @Column(name = "reservation_id")
    // private Long reservationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @Column(name = "order_type", length = 20)
    private String orderType;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "final_amount", precision = 10, scale = 2)
    private BigDecimal finalAmount;

    @Column(name = "points_used")
    private Integer pointsUsed;

    @Column(name = "points_earned")
    private Integer pointsEarned;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "invoice_type", length = 30)
    private String invoiceType;

    @Column(name = "carrier_number", length = 20)
    private String carrierNumber;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
