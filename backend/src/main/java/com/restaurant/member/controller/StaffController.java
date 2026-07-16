package com.restaurant.member.controller;

import com.restaurant.common.ApiResponse;
import com.restaurant.member.dto.StaffResponse;
import com.restaurant.member.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members/staff")
@RequiredArgsConstructor
public class StaffController {

    private final UserService userService;

    /**
     * 查詢所有員工（含離職）
     * GET /api/members/staff
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<StaffResponse>>> getActiveStaffs() {

        List<StaffResponse> data = userService.getActiveStaffs();
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 查詢特定員工
     * GET /api/members/staff/{staffId}
     */

    @GetMapping("/{staffId}")
    public ResponseEntity<ApiResponse<StaffResponse>> getStaff(@PathVariable Long staffId) {
        StaffResponse data = userService.getStaff(staffId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 員工離職
     * PUT /api/members/staff/{staffId}/resign
     */
    @PutMapping("/{staffId}/resign")
    public ResponseEntity<ApiResponse<Void>> resignStaff(
            @PathVariable Long staffId) {

        userService.resignStaff(staffId);
        return ResponseEntity.ok(ApiResponse.success("員工狀態已更新為離職"));
    }
}