package com.restaurant.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import com.restaurant.store.entity.Store;
import java.time.LocalDateTime;

/**
 * 會員點數異動歷史紀錄實體，對應 point_transaction 資料表。
 * 負責記錄所有點數的獲取、扣除、過期或手動調整之流水帳。
 */
@Entity
@Table(name = "point_transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointTransaction {

    public enum TransactionType {
        EARN, USE, EXPIRE, ADJUST
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tx_id")
    private Long txId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Column(name = "point_change", nullable = false)
    private Integer pointChange;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, columnDefinition = "varchar(20)")
    private TransactionType transactionType;

    @Column(name = "reference_id")
    private Long referenceId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}