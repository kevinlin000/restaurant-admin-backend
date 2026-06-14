package com.restaurant.member.service.impl;

import com.restaurant.common.BusinessException;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.restaurant.member.service.MailService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

        private final UserRepository userRepository;
        private final MemberProfileRepository memberProfileRepository;
        private final StaffRepository staffRepository;
        private final RoleRepository roleRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtil jwtUtil;
        private final MailService mailService;
        private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();

        // -----Member帳號註冊（核心 User + 1:1 MemberProfile）-----
        @Override
        @Transactional
        public LoginResponse registerMember(MemberRegisterRequest request) {
                // 檢查Email是否重複
                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new BusinessException("此 Email 已被註冊");
                }
                // 檢查手機號碼是否重複
                if (userRepository.existsByPhone(request.getPhone())) {
                        throw new BusinessException("此手機號碼已被使用");
                }

                // 撈取資料庫中的預設角色
                Role memberRole = roleRepository.findByRoleName("CUSTOMER")
                                .orElseThrow(() -> new BusinessException("系統角色 CUSTOMER 不存在"));

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
        public StaffResponse createStaff(StaffCreateRequest request) {

                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new BusinessException("此 Email 已被使用");
                }

                Role role = roleRepository.findByRoleName(request.getRoleName())
                                .orElseThrow(() -> new BusinessException("指定的系統角色不存在：" + request.getRoleName()));

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
                                .orElseThrow(() -> new BusinessException("找不到帳號"));

                boolean passwordMatched = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());

                if (!passwordMatched) {
                        throw new BusinessException("帳號或密碼錯誤");
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

        @Override
        public void sendEmailVerificationCode(String email) {

                String code = String.valueOf(
                                (int) ((Math.random() * 900000) + 100000));

                verificationCodes.put(email, code);

                mailService.sendEmail(
                                email,
                                "敘日會員驗證信",
                                "您好，您的驗證碼為：" + code);
        }

        @Override
        public boolean verifyEmailCode(String email, String code) {

                String savedCode = verificationCodes.get(email);

                return savedCode != null &&
                                savedCode.equals(code);
        }

        @Override
        public void forgotPassword(String email) {

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new BusinessException("此 Email 不存在"));

                sendEmailVerificationCode(user.getEmail());
        }

        @Override
        @Transactional
        public void resetPassword(
                        String email,
                        String code,
                        String newPassword) {

                if (!verifyEmailCode(email, code)) {
                        throw new BusinessException("驗證碼錯誤");
                }

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new BusinessException("找不到帳號"));

                user.setPasswordHash(
                                passwordEncoder.encode(newPassword));

                userRepository.save(user);

                verificationCodes.remove(email);
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