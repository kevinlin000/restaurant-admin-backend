package com.restaurant.member.controller;

import com.restaurant.common.ApiResponse;
import com.restaurant.member.dto.MemberAdminSummaryResponse;
import com.restaurant.member.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Member 模組提供給後台首頁使用的統計 API。
 * 不依賴訂單或訂位模組，避免跨組開發時互相阻塞。
 */
@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
public class MemberAdminController {

    private final UserService userService;

    /**
     * 後台首頁會員 / 員工摘要
     * GET /api/admin/members/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<MemberAdminSummaryResponse>> getSummary() {
        MemberAdminSummaryResponse data = userService.getAdminSummary();
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
