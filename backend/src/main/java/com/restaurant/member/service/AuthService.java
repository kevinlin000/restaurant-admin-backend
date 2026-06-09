package com.restaurant.member.service;

import com.restaurant.member.dto.LoginRequest;
import com.restaurant.member.dto.MemberRegisterRequest;
import com.restaurant.member.dto.StaffCreateRequest;
import com.restaurant.member.dto.StaffResponse;

public interface AuthService {

    /**
     * 會員註冊：建立核心帳號 User + 初始化 MemberProfile
     */
    String registerMember(MemberRegisterRequest request);

    /**
     * 員工註冊：由管理員建立核心 User + 初始化 Staff
     */
    StaffResponse createStaff(StaffCreateRequest request, String roleName);

    /**
     * 統一登入入口：驗證帳密後，依角色回傳 JWT Access Token
     */
    String login(LoginRequest request);
}
