package com.restaurant.store.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalTime;

@Data
public class StoreHourCreateRequest {

    @NotNull(message = "星期不可為空")
    @Min(value = 1, message = "星期需介於 1 到 7")
    @Max(value = 7, message = "星期需介於 1 到 7")
    private Integer dayOfWeek;

    @NotNull(message = "開店時間不可為空")
    private LocalTime openTime;

    @NotNull(message = "關店時間不可為空")
    private LocalTime closeTime;

    @Pattern(regexp = "LUNCH|DINNER|AFTERNOON_TEA|ALL_DAY", message = "營業時段不合法")
    private String mealPeriod = "ALL_DAY";

    private Boolean isClosed = false;
}
