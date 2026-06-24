package com.restaurant.reservation.controller;

import com.restaurant.common.ApiResponse;
import com.restaurant.reservation.dto.CreateReservationRequest;
import com.restaurant.reservation.dto.ReservationResponse;
import com.restaurant.reservation.entity.ReservationCapacity;
import com.restaurant.reservation.entity.TimeSlot;
import com.restaurant.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // 顧客端訂位頁 -> 可訂日期/時段，依店家設定的 time_slot
    @GetMapping("/slots")
    public ApiResponse<List<TimeSlot>> getAvailableSlots(
            @RequestParam Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ApiResponse.success(reservationService.getAvailableSlots(storeId, startDate, endDate));
    }

    // 選時段後 -> 查剩餘容量，判斷是否還能訂
    @GetMapping("/slots/{slotId}/capacity")
    public ApiResponse<List<ReservationCapacity>> getAvailableCapacity(@PathVariable Long slotId) {
        return ApiResponse.success(reservationService.getAvailableCapacity(slotId));
    }

    // 送出訂位 -> 後端建立 reservation 並扣 reservation_capacity
    @PostMapping
    public ApiResponse<ReservationResponse> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        return ApiResponse.success("訂位成功", reservationService.createReservation(request));
    }

    // 信件連結 -> 沒有登入也可透過訂位 access token 讀取成功頁資料
    @GetMapping("/{reservationId}/public")
    public ApiResponse<ReservationResponse> getPublicReservation(
            @PathVariable Long reservationId,
            @RequestParam String token
    ) {
        return ApiResponse.success(reservationService.getReservationByAccessToken(reservationId, token));
    }

    // 訂位成功、編輯頁 -> 讀取單筆訂位
    @GetMapping("/{reservationId}")
    public ApiResponse<ReservationResponse> getReservation(@PathVariable Long reservationId) {
        return ApiResponse.success(reservationService.getReservation(reservationId));
    }

    // 修改訂位資訊 -> 顧客在 PENDING 狀態下
    @PutMapping("/{reservationId}")
    public ApiResponse<ReservationResponse> updateReservation(
            @PathVariable Long reservationId,
            @Valid @RequestBody CreateReservationRequest request
    ) {
        return ApiResponse.success("訂位已更新", reservationService.updateReservation(reservationId, request));
    }

    // 訂位成功頁 -> 確認保留訂位
    @PatchMapping("/{reservationId}/reserve")
    public ApiResponse<ReservationResponse> reserveReservation(@PathVariable Long reservationId) {
        return ApiResponse.success("訂位已保留", reservationService.reserveReservation(reservationId));
    }

    // 取消訂位 -> 釋放容量並將狀態改為 CANCELLED
    @DeleteMapping("/{reservationId}")
    public ApiResponse<Void> cancelReservation(@PathVariable Long reservationId) {
        reservationService.cancelReservation(reservationId);
        return ApiResponse.success("訂位已取消");
    }
}
