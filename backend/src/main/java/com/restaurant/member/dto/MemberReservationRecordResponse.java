package com.restaurant.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 會員中心訂位紀錄回應資料。
 *
 * 只給 member/profile.vue 顯示使用，避免為了會員頁面修改 reservation 模組既有 DTO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberReservationRecordResponse {
    private Long reservationId;
    private Long userId;
    private Long storeId;
    private String storeName;
    private Long slotId;
    private LocalDate reservationDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer partySize;
    private String status;
    private String paymentStatus;
    private String specialRequest;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private LocalDateTime createdAt;
}
