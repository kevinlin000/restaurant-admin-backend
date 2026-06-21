package com.restaurant.reservation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class TimeSlotRequest {

    @NotNull(message = "分店 ID 必填")
    private Long storeId;

    @NotNull(message = "訂位日期必填")
    private LocalDate reservationDate;

    @NotNull(message = "開始時間必填")
    private LocalTime startTime;

    @NotNull(message = "結束時間必填")
    private LocalTime endTime;

    // 是否開放預訂；預設開放。
    private Boolean isOpen = true;

    // 是否由星期規則批次產生；自訂日期或單日修改會是 false。
    private Boolean ruleGenerated = false;
}
