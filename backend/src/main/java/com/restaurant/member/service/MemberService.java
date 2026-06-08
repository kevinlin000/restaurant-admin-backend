package com.restaurant.member.service;

import com.restaurant.member.dto.*;
import com.restaurant.member.entity.*;
import com.restaurant.member.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public MemberProfileResponse register(MemberRegisterRequest request) {
        // 1. 基本防呆
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("該 Email 已被註冊！");
        }
        Role memberRole = roleRepository.findByRoleName("MEMBER")
                .orElseThrow(() -> new IllegalStateException("系統找不到 MEMBER 角色"));

        // 2. 建立 User 並直接連動綁定 MemberProfile
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(request.getPassword())
                .name(request.getName())
                .phone(request.getPhone())
                .birthday(request.getBirthday())
                .role(memberRole)
                .build();

        user.setMemberProfile(MemberProfile.builder()
                .user(user)
                .build()); // 點數預設 0、等級預設 BRONZE 都交給 Entity 處理

        return convertToProfileResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public MemberProfileResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("帳號或密碼錯誤！"));

        if (!user.getPasswordHash().equals(request.getPassword())) {
            throw new IllegalArgumentException("帳號或密碼錯誤！");
        }

        return convertToProfileResponse(user);
    }

    // 負責把 Entity 轉成 Response DTO 的小幫手
    private MemberProfileResponse convertToProfileResponse(User user) {
        MemberProfileResponse res = new MemberProfileResponse();
        res.setUserId(user.getUserId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setPhone(user.getPhone());
        res.setBirthday(user.getBirthday());
        res.setCreatedAt(user.getCreatedAt());

        if (user.getMemberProfile() != null) {
            res.setPointBalance(user.getMemberProfile().getPointBalance());
            res.setMemberLevel(user.getMemberProfile().getMemberLevel());
        }
        return res;
    }
}