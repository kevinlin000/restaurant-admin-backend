package com.restaurant.reservation.controller;

import com.restaurant.common.ApiResponse;
import com.restaurant.reservation.dto.AssignTableRequest;
import com.restaurant.reservation.dto.CreateReservationRequest;
import com.restaurant.reservation.dto.DailyOverviewResponse;
import com.restaurant.reservation.dto.ReservationResponse;
import com.restaurant.reservation.service.ReservationAdminService;
import com.restaurant.store.service.StoreAdminAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
public class ReservationAdminController {

    private final ReservationAdminService reservationAdminService;
    private final StoreAdminAccessService storeAdminAccessService;

    // 訂位名單頁：查全部訂位
    @GetMapping
    public ApiResponse<List<ReservationResponse>> getReservationList(@RequestParam Long storeId, Authentication authentication) {
        storeAdminAccessService.requireStoreAccess(authentication, storeId);
        return ApiResponse.success(reservationAdminService.getReservationList(storeId));
    }

    // 分配桌位頁：查待配桌訂位，可依日期與狀態篩選
    @GetMapping("/unassigned")
    public ApiResponse<List<ReservationResponse>> getUnassignedReservations(
            @RequestParam Long storeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String status,
            Authentication authentication
    ) {
        storeAdminAccessService.requireStoreAccess(authentication, storeId);
        return ApiResponse.success(reservationAdminService.getUnassignedReservations(storeId, date, status));
    }

    // 訂位總覽：查單日統計與單日訂位清單(前端合併成近三日名單）
    @GetMapping("/daily-overview")
    public ApiResponse<DailyOverviewResponse> getDailyOverview(
            @RequestParam Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication
    ) {
        storeAdminAccessService.requireStoreAccess(authentication, storeId);
        return ApiResponse.success(reservationAdminService.getDailyOverview(storeId, date));
    }

    // 分配、重新分配桌位
    @PostMapping("/assign-tables")
    public ApiResponse<ReservationResponse> assignTables(@Valid @RequestBody AssignTableRequest request, Authentication authentication) {
        storeAdminAccessService.requireStoreAccess(authentication, reservationAdminService.getReservationStoreId(request.getReservationId()));
        return ApiResponse.success("桌位已分配", reservationAdminService.assignTables(request));
    }

    // 確認保留訂位(店家端)
    @PatchMapping("/reserve")
    public ApiResponse<ReservationResponse> reserve(@RequestParam Long reservationId, Authentication authentication) {
        storeAdminAccessService.requireStoreAccess(authentication, reservationAdminService.getReservationStoreId(reservationId));
        return ApiResponse.success("已確認保留", reservationAdminService.reserve(reservationId));
    }

    // 勾選實際入座
    @PatchMapping("/check-in")
    public ApiResponse<ReservationResponse> checkIn(@RequestParam Long reservationId, Authentication authentication) {
        storeAdminAccessService.requireStoreAccess(authentication, reservationAdminService.getReservationStoreId(reservationId));
        return ApiResponse.success("已標記入座", reservationAdminService.checkIn(reservationId));
    }

    // 編輯訂位
    @PutMapping("/{reservationId}")
    public ApiResponse<ReservationResponse> updateReservationInfo(
            @PathVariable Long reservationId,
            @Valid @RequestBody CreateReservationRequest request,
            Authentication authentication
    ) {
        storeAdminAccessService.requireStoreAccess(authentication, reservationAdminService.getReservationStoreId(reservationId));
        return ApiResponse.success("訂位資料已更新", reservationAdminService.updateReservationInfo(reservationId, request));
    }
}
