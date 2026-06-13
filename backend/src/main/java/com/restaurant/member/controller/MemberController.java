package com.restaurant.member.controller;

import com.restaurant.common.ApiResponse;
import com.restaurant.member.dto.MemberProfileResponse;
import com.restaurant.member.dto.MemberUpdateRequest;
import com.restaurant.member.dto.PasswordUpdateRequest;
import com.restaurant.member.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final UserService userService;

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
