package com.restaurant.store.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "store_hour")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreHour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hour_id")
    private Long hourId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Column(name = "meal_period", length = 20)
    private String mealPeriod;

    @Column(name = "is_closed", nullable = false)
    @Builder.Default
    private Boolean isClosed = false;
}
