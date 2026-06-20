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

import java.time.LocalDateTime;

@Entity
@Table(name = "reservation_capacity")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCapacity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "capacity_id")
    private Long capacityId;

    @Column(name = "slot_id", nullable = false)
    private Long slotId;

    @Column(name = "table_size", nullable = false)
    private Integer tableSize;

    @Column(name = "total_count", nullable = false)
    private Integer totalCount;

    @Column(name = "reserved_count", nullable = false)
    @Builder.Default
    private Integer reservedCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 判斷是否還有剩餘容量，避免建立超過 total_count 的訂位
    public boolean hasRemaining() {
        return reservedCount < totalCount;
    }
}
