package com.restaurant.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {

    // 訂位欄位 -> reservation、time_slot、reservation_table 整合在一起
    private Long reservationId;
    private Long userId;
    private Long storeId;
    private Long slotId;

    // 日期與時間 time_slot
    private LocalDate reservationDate;
    private LocalTime startTime;
    private LocalTime endTime;

    // 其他訂位基本資料、狀態
    private Integer partySize;
    private String status;
    private BigDecimal depositAmount;
    private String paymentStatus;
    private String specialRequest;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String accessToken;

    // 已分配桌位 ID、桌號 -> 訂位總覽、訂位名單、分配桌位頁顯示。
    private List<Long> tableIds;
    private List<String> tableNumbers;
    private LocalDateTime createdAt;
}
