package com.restaurant.store.service;

import com.restaurant.member.entity.Staff;
import com.restaurant.member.entity.Staff.StaffStatus;
import com.restaurant.member.repository.StaffRepository;
import com.restaurant.store.dto.response.StoreListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StoreAdminAccessService {

    private final StaffRepository staffRepository;

    public List<StoreListResponse> filterManageableStores(Authentication authentication, List<StoreListResponse> stores) {
        if (hasAuthority(authentication, "ROLE_ADMIN")) {
            return stores;
        }

        Long storeId = currentStaffStoreId(authentication);
        return stores.stream()
                .filter(store -> Objects.equals(store.getStoreId(), storeId))
                .toList();
    }

    public void requireAdmin(Authentication authentication) {
        if (!hasAuthority(authentication, "ROLE_ADMIN")) {
            throw new AccessDeniedException("此操作僅限系統管理員");
        }
    }

    public void requireStoreAccess(Authentication authentication, Long storeId) {
        if (hasAuthority(authentication, "ROLE_ADMIN")) {
            return;
        }

        Long staffStoreId = currentStaffStoreId(authentication);
        if (!Objects.equals(staffStoreId, storeId)) {
            throw new AccessDeniedException("只能管理自己所屬門市");
        }
    }

    private Long currentStaffStoreId(Authentication authentication) {
        if (!hasAuthority(authentication, "ROLE_MANAGER")) {
            throw new AccessDeniedException("沒有後台門市管理權限");
        }

        Long userId = currentUserId(authentication);
        Staff staff = staffRepository.findByUser_UserIdAndStatus(userId, StaffStatus.ACTIVE)
                .orElseThrow(() -> new AccessDeniedException("找不到在職員工門市資料"));
        return staff.getStore().getStoreId();
    }

    private Long currentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AccessDeniedException("尚未登入");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Long userId) {
            return userId;
        }
        if (principal instanceof Number number) {
            return number.longValue();
        }
        if (principal instanceof String text) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ignored) {
                throw new AccessDeniedException("登入資訊格式錯誤");
            }
        }
        throw new AccessDeniedException("登入資訊格式錯誤");
    }

    private boolean hasAuthority(Authentication authentication, String authority) {
        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> authority.equals(grantedAuthority.getAuthority()));
    }
}
