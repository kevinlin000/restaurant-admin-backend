package com.restaurant.store.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "table_info")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "table_id")
    private Long tableId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "table_number", nullable = false, length = 10)
    private String tableNumber;

    @Column(name = "table_size", nullable = false)
    private Integer tableSize;

    @Column(name = "table_type", length = 20)
    private String tableType;

    @Column(name = "zone", length = 20)
    private String zone;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "AVAILABLE";

    @Column(name = "is_combinable", nullable = false)
    @Builder.Default
    private Boolean isCombinable = false;
}
