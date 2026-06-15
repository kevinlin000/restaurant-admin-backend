package com.restaurant.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreHourResponse {
    private Long hourId;
    private Integer dayOfWeek;
    private String dayName;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String mealPeriod;
    private Boolean isClosed;
}
