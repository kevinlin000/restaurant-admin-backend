package com.restaurant.member.controller;

import com.restaurant.common.ApiResponse;
import com.restaurant.member.dto.ForgotPasswordRequest;
import com.restaurant.member.dto.LoginRequest;
import com.restaurant.member.dto.LoginResponse;
import com.restaurant.member.dto.MemberRegisterRequest;
import com.restaurant.member.dto.ResetPasswordRequest;
import com.restaurant.member.dto.StaffCreateRequest;
import com.restaurant.member.dto.StaffResponse;
import com.restaurant.member.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 會員註冊
     * POST /api/members/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(
            @Valid @RequestBody MemberRegisterRequest request) {

        LoginResponse data = authService.registerMember(request);
        return ResponseEntity.ok(ApiResponse.success("註冊成功", data));
    }

    /**
     * 統一登入入口（會員與員工共用）
     * POST /api/members/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse data = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("登入成功", data));
    }

    /**
     * 建立員工帳號（由 ADMIN 操作）
     * POST /api/members/staff
     * 
     * @param roleName 指定員工角色，例如 STAFF 或 MANAGER，從 Header 傳入
     */
    @PostMapping("/staff")
    public ResponseEntity<ApiResponse<StaffResponse>> createStaff(
            @Valid @RequestBody StaffCreateRequest request) {

        StaffResponse data = authService.createStaff(request);
        return ResponseEntity.ok(ApiResponse.success("員工帳號建立成功", data));
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(
                request.getEmail());

        return ResponseEntity.ok(
                ApiResponse.success("驗證碼已寄出"));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(
                request.getEmail(),
                request.getCode(),
                request.getNewPassword());

        return ResponseEntity.ok(
                ApiResponse.success("密碼重設成功"));
    }
}