package com.restaurant.reservation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateReservationRequest {

    // 顧客登入帶 userId；未登入可為 null
    private Long userId;

    @NotNull(message = "分店 ID 必填")
    private Long storeId;

    @NotNull(message = "時段 ID 必填")
    private Long slotId;

    // 未登入訂位需要姓名、電話
    @NotBlank(message = "姓名必填")
    private String customerName;

    @NotBlank(message = "電話必填")
    private String customerPhone;

    @Email(message = "Email 格式不正確")
    private String customerEmail;

    @NotNull(message = "人數必填")
    @Positive(message = "人數必須大於 0")
    private Integer partySize;

    // 特殊需求
    private String specialRequest;
}
