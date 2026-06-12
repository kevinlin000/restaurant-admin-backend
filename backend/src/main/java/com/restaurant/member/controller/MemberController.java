package com.restaurant.member.controller;

import com.restaurant.common.ApiResponse;
import com.restaurant.member.dto.MemberProfileResponse;
import com.restaurant.member.dto.MemberUpdateRequest;
import com.restaurant.member.dto.PasswordUpdateRequest;
import com.restaurant.member.service.UserService;
import com.restaurant.member.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 查詢自己的個人資料
     * GET /api/members/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> getProfile(
            @RequestHeader("Authorization") String authHeader) {

        Long userId = getUserIdFromHeader(authHeader);
        MemberProfileResponse data = userService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 修改個人資料
     * PUT /api/members/me
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody MemberUpdateRequest request) {

        Long userId = getUserIdFromHeader(authHeader);
        MemberProfileResponse data = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("個人資料更新成功", data));
    }

    /**
     * 修改密碼
     * PUT /api/members/me/password
     */
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody PasswordUpdateRequest request) {

        Long userId = getUserIdFromHeader(authHeader);
        userService.updatePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success("密碼修改成功"));
    }

    /**
     * 註銷帳號（軟刪除）
     * DELETE /api/members/me
     */
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@RequestHeader("Authorization") String authHeader) {

        userService.deleteAccount(getUserIdFromHeader(authHeader));

        return ResponseEntity.ok(ApiResponse.success("帳號已註銷"));
    }

    private Long getUserIdFromHeader(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.getUserId(token);
    }
}