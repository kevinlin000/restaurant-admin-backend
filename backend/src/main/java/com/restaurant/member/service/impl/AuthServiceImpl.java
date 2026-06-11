package com.restaurant.member.service.impl;

import com.restaurant.member.dto.LoginRequest;
import com.restaurant.member.dto.LoginResponse;
import com.restaurant.member.dto.MemberRegisterRequest;
import com.restaurant.member.dto.StaffCreateRequest;
import com.restaurant.member.dto.StaffResponse;
import com.restaurant.member.entity.*;
import com.restaurant.member.repository.*;
import com.restaurant.member.service.AuthService;
import com.restaurant.member.util.JwtUtil;
import com.restaurant.store.entity.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final StaffRepository staffRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // -----Member帳號註冊（核心 User + 1:1 MemberProfile）-----
    @Override
    @Transactional
    public LoginResponse registerMember(MemberRegisterRequest request) {
        // 檢查Email是否重複
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("此 Email 已被註冊");
        }
        // 檢查手機號碼是否重複
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("此手機號碼已被使用");
        }

        // 撈取資料庫中的預設角色
        Role memberRole = roleRepository.findByRoleName("CUSTOMER")
                .orElseThrow(() -> new IllegalStateException("系統角色 CUSTOMER 不存在"));

        // 建立User帳號
        User user = User.builder()
                .role(memberRole)
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .birthday(request.getBirthday())
                .build();

        userRepository.save(user);

        MemberProfile profile = MemberProfile.builder()
                .user(user)
                .build();
        memberProfileRepository.save(profile);

        // 註冊完直接產生 token，回傳完整的 LoginResponse
        String token = jwtUtil.generateToken(user.getUserId(), user.getRole().getRoleName());
        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getUserId())
                .name(user.getName())
                .roleName(user.getRole().getRoleName())
                .build();
    }

    // -----Staff帳號建立（後台管理者幫員工建立核心 User + 1:1 Staff）-----
    @Override
    @Transactional
    public StaffResponse createStaff(StaffCreateRequest request, String roleName) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("此 Email 已被使用");
        }

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("指定的系統角色不存在：" + roleName));

        Store store = Store.builder().storeId(request.getStoreId()).build();

        User user = User.builder()
                .role(role)
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword())) // 預設密碼也要加密！
                .name(request.getName())
                .phone(request.getPhone())
                .build();
        userRepository.save(user);

        Staff staff = Staff.builder()
                .user(user)
                .store(store)
                .staffNo(request.getStaffNo())
                .hireDate(request.getHireDate())
                .build();
        staffRepository.save(staff);

        return toStaffResponse(user, staff);
    }

    // -----統一登入入口（驗證並依據角色簽發JWT通行證）-----

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("帳號或密碼錯誤"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("帳號或密碼錯誤");
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getRole().getRoleName());
        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getUserId())
                .name(user.getName())
                .roleName(user.getRole().getRoleName())
                .build();
    }

    /**
     * 封裝員工回傳格式
     */
    private StaffResponse toStaffResponse(User user, Staff staff) {
        StaffResponse res = new StaffResponse();
        res.setUserId(user.getUserId());
        res.setStaffId(staff.getStaffId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setPhone(user.getPhone());
        res.setRoleName(user.getRole().getRoleName());
        res.setStoreId(staff.getStore().getStoreId());
        res.setStaffNo(staff.getStaffNo());
        res.setHireDate(staff.getHireDate());
        res.setStatus(staff.getStatus() != null ? staff.getStatus().name() : null);
        return res;
    }
}