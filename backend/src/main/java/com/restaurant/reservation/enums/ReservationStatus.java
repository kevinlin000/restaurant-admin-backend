package com.restaurant.reservation.enums;

public enum ReservationStatus {

    PENDING, // 訂位成功，尚未確認保留

    RESERVED, // 已確認保留，尚未配桌

    ASSIGNED, // 已配桌，尚未入座

    CHECKED_IN, // 已入座，不能再編輯、取消或改桌位

    COMPLETED, // 用餐流程完成

    NO_SHOW, // 顧客未到

    CANCELLED // 訂位已取消，不列入訂位總數統計
}
