package com.restaurant.reservation.controller;

import com.restaurant.common.ApiResponse;
import com.restaurant.reservation.dto.TimeSlotRequest;
import com.restaurant.reservation.entity.ReservationCapacity;
import com.restaurant.reservation.entity.TimeSlot;
import com.restaurant.reservation.service.ReservationAdminAccessService;
import com.restaurant.reservation.service.ReservationSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/admin/reservation-settings")
@RequiredArgsConstructor
public class ReservationSettingController {

    private final ReservationSettingService reservationSettingService;
    private final ReservationAdminAccessService reservationAdminAccessService;

    // 查詢可訂時段列表
    @GetMapping("/time-slots")
    public ApiResponse<List<TimeSlot>> getTimeSlots(
            @RequestParam Long storeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication
    ) {
        reservationAdminAccessService.requireStoreAccess(authentication, storeId);
        return ApiResponse.success(reservationSettingService.getTimeSlots(storeId, date));
    }

    // 新增訂位時段，依分店桌位同步產生容量
    @PostMapping("/time-slots")
    public ApiResponse<TimeSlot> createTimeSlot(@Valid @RequestBody TimeSlotRequest request, Authentication authentication) {
        reservationAdminAccessService.requireManagerOrAdmin(authentication);
        reservationAdminAccessService.requireStoreAccess(authentication, request.getStoreId());
        return ApiResponse.success("訂位時段已新增", reservationSettingService.createTimeSlot(request));
    }

    // 修改訂位時段
    @PutMapping("/time-slots/{slotId}")
    public ApiResponse<TimeSlot> updateTimeSlot(
            @PathVariable Long slotId,
            @Valid @RequestBody TimeSlotRequest request,
            Authentication authentication
    ) {
        reservationAdminAccessService.requireManagerOrAdmin(authentication);
        reservationAdminAccessService.requireStoreAccess(authentication, reservationSettingService.getTimeSlotStoreId(slotId));
        reservationAdminAccessService.requireStoreAccess(authentication, request.getStoreId());
        return ApiResponse.success("訂位時段已更新", reservationSettingService.updateTimeSlot(slotId, request));
    }

    // 刪除訂位時段。
    @DeleteMapping("/time-slots/{slotId}")
    public ApiResponse<Void> deleteTimeSlot(@PathVariable Long slotId, Authentication authentication) {
        reservationAdminAccessService.requireManagerOrAdmin(authentication);
        reservationAdminAccessService.requireStoreAccess(authentication, reservationSettingService.getTimeSlotStoreId(slotId));
        reservationSettingService.deleteTimeSlot(slotId);
        return ApiResponse.success("訂位時段已刪除");
    }

    // 重新計算時段容量
    @PostMapping("/time-slots/{slotId}/rebuild-capacity")
    public ApiResponse<Void> rebuildCapacity(@PathVariable Long slotId, Authentication authentication) {
        reservationAdminAccessService.requireManagerOrAdmin(authentication);
        reservationAdminAccessService.requireStoreAccess(authentication, reservationSettingService.getTimeSlotStoreId(slotId));
        reservationSettingService.rebuildCapacity(slotId);
        return ApiResponse.success("容量已重新計算");
    }

    // 查某時段容量，顯示總數、已訂、剩餘。
    @GetMapping("/time-slots/{slotId}/capacity")
    public ApiResponse<List<ReservationCapacity>> getCapacity(@PathVariable Long slotId, Authentication authentication) {
        reservationAdminAccessService.requireStoreAccess(authentication, reservationSettingService.getTimeSlotStoreId(slotId));
        return ApiResponse.success(reservationSettingService.getCapacity(slotId));
    }
}
