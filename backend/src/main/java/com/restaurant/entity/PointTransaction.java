package com.restaurant.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "point_transaction")
@Data
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tx_id")
    private Long txId; // 交易紀錄唯一流水號

    @ManyToOne(fetch = FetchType.LAZY) // 懶加載，需要時才從資料庫撈出來
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude // @Data 生成 toString 時跳過這個欄位
    @EqualsAndHashCode.Exclude // 避免物件比較時觸發多餘的加載
    private User user;

    @Column(name = "store_id", nullable = false)
    private Long storeId; // 在哪家分店發生這次點數變動

    @Column(name = "point_change", nullable = false)
    private BigDecimal pointChange; // 點數變動值

    @Column(name = "transaction_type", nullable = false)
    private String transactionType; // 變動類型：例如 "EARN" (消費回饋), "SPEND" (點數折抵), "REFUND" (退貨退點)

    @Column(name = "reference_id")
    private Long referenceId; // 紀錄這次點數變動的 `order_id`

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 點數變動發生時間
}