package com.restaurant.member.controller;

import com.restaurant.common.ApiResponse;
import com.restaurant.member.dto.MemberProfileResponse;
import com.restaurant.member.dto.MemberReservationRecordResponse;
import com.restaurant.member.dto.MemberUpdateRequest;
import com.restaurant.member.dto.PasswordUpdateRequest;
import com.restaurant.member.service.UserService;
import com.restaurant.order.dto.OrderSummaryResponse;
import com.restaurant.order.service.OrderService;
import com.restaurant.reservation.dto.ReservationResponse;
import com.restaurant.reservation.entity.Reservation;
import com.restaurant.reservation.repository.ReservationRepository;
import com.restaurant.reservation.service.ReservationService;
import com.restaurant.store.entity.Store;
import com.restaurant.store.repository.StoreRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final UserService userService;
    private final OrderService orderService;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    private final StoreRepository storeRepository;

    /**
     * 查詢自己的個人資料
     * GET /api/members/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> getProfile() {
        Long userId = getCurrentUserId();
        MemberProfileResponse data = userService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 查詢自己的消費紀錄
     * GET /api/members/me/orders
     */
    @GetMapping("/me/orders")
    public ResponseEntity<ApiResponse<List<OrderSummaryResponse>>> getMyOrders() {
        Long userId = getCurrentUserId();
        List<OrderSummaryResponse> data = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 查詢自己的訂位紀錄
     * GET /api/members/me/reservations
     */
    @GetMapping("/me/reservations")
    public ResponseEntity<ApiResponse<List<MemberReservationRecordResponse>>> getMyReservations() {
        Long userId = getCurrentUserId();

        List<Reservation> reservations = reservationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<Long> storeIds = reservations.stream()
                .map(Reservation::getStoreId)
                .distinct()
                .toList();
        Map<Long, Store> storeById = storeRepository.findAllById(storeIds).stream()
                .collect(Collectors.toMap(Store::getStoreId, Function.identity()));

        List<MemberReservationRecordResponse> data = reservations.stream()
                .map(reservation -> {
                    ReservationResponse reservationResponse = reservationService.toResponse(reservation);
                    Store store = storeById.get(reservationResponse.getStoreId());

                    return MemberReservationRecordResponse.builder()
                            .reservationId(reservationResponse.getReservationId())
                            .userId(reservationResponse.getUserId())
                            .storeId(reservationResponse.getStoreId())
                            .storeName(store == null ? null : store.getStoreName())
                            .slotId(reservationResponse.getSlotId())
                            .reservationDate(reservationResponse.getReservationDate())
                            .startTime(reservationResponse.getStartTime())
                            .endTime(reservationResponse.getEndTime())
                            .partySize(reservationResponse.getPartySize())
                            .status(reservationResponse.getStatus())
                            .paymentStatus(reservationResponse.getPaymentStatus())
                            .specialRequest(reservationResponse.getSpecialRequest())
                            .customerName(reservationResponse.getCustomerName())
                            .customerPhone(reservationResponse.getCustomerPhone())
                            .customerEmail(reservationResponse.getCustomerEmail())
                            .createdAt(reservationResponse.getCreatedAt())
                            .build();
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 修改個人資料
     * PUT /api/members/me
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> updateProfile(
            @Valid @RequestBody MemberUpdateRequest request) {
        Long userId = getCurrentUserId();
        MemberProfileResponse data = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("個人資料更新成功", data));
    }

    /**
     * 修改密碼
     * PUT /api/members/me/password
     */
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @Valid @RequestBody PasswordUpdateRequest request) {
        Long userId = getCurrentUserId();
        userService.updatePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success("密碼修改成功"));
    }

    /**
     * 註銷帳號（軟刪除）
     * DELETE /api/members/me
     */
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteAccount() {
        userService.deleteAccount(getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("帳號已註銷"));
    }

    /**
     * 從 SecurityContextHolder 取得當前登入者的 userId。
     * JwtAuthenticationFilter 在驗證 Token 後會將 userId 存入 principal，
     * 所以這裡直接取用，不需要再手動解析 Token。
     */
    private Long getCurrentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
