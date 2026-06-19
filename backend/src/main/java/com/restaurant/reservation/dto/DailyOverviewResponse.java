package com.restaurant.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyOverviewResponse {

    // 訂位總覽單日統計 -> 前端會把三日結果合併成近三日名單
    private Long storeId;
    private String date;

    private long totalCount; // totalCount 不包含 CANCELLED
    private long assignedCount;
    private long unassignedCount;
    private long checkedInCount;

    // 單日訂位明細
    private List<ReservationResponse> reservations;
}
