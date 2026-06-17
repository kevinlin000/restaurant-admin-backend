package com.restaurant.member.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.common.ApiResponse;
import com.restaurant.member.dto.PointBalanceResponse;
import com.restaurant.member.dto.PointTransactionResponse;
import com.restaurant.member.service.PointService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members/me/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    /**
     * 查詢目前會員點數、會員等級與距離下一級還差幾點。
     * GET /api/members/me/points
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PointBalanceResponse>> getPointBalance() {
        Long userId = getCurrentUserId();
        PointBalanceResponse data = pointService.getPointBalance(userId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 查詢目前會員點數異動紀錄。
     * GET /api/members/me/points/history
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<PointTransactionResponse>>> getPointHistory() {
        Long userId = getCurrentUserId();
        List<PointTransactionResponse> data = pointService.getPointHistory(userId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    private Long getCurrentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
