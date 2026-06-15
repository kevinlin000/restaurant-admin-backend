package com.restaurant.store.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "store_holiday")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreHoliday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holiday_id")
    private Long holidayId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @Column(name = "reason", length = 100)
    private String reason;
}
