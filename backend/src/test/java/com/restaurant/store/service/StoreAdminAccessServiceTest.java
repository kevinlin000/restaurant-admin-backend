package com.restaurant.store.service;

import com.restaurant.member.entity.Staff;
import com.restaurant.member.repository.StaffRepository;
import com.restaurant.store.dto.response.StoreListResponse;
import com.restaurant.store.entity.Store;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreAdminAccessServiceTest {

    @Mock
    private StaffRepository staffRepository;

    @InjectMocks
    private StoreAdminAccessService storeAdminAccessService;

    @Test
    void adminCanSeeAllStoresWithoutStaffLookup() {
        List<StoreListResponse> stores = List.of(storeResponse(1L), storeResponse(2L));

        List<StoreListResponse> result = storeAdminAccessService.filterManageableStores(adminAuth(), stores);

        assertThat(result).extracting(StoreListResponse::getStoreId).containsExactly(1L, 2L);
        verifyNoInteractions(staffRepository);
    }

    @Test
    void managerCanOnlySeeAssignedStore() {
        when(staffRepository.findByUser_UserIdAndStatus(10L, Staff.StaffStatus.ACTIVE))
                .thenReturn(Optional.of(staff(2L)));

        List<StoreListResponse> result = storeAdminAccessService.filterManageableStores(
                managerAuth(10L),
                List.of(storeResponse(1L), storeResponse(2L), storeResponse(3L))
        );

        assertThat(result).extracting(StoreListResponse::getStoreId).containsExactly(2L);
    }

    @Test
    void managerCanAccessAssignedStore() {
        when(staffRepository.findByUser_UserIdAndStatus(10L, Staff.StaffStatus.ACTIVE))
                .thenReturn(Optional.of(staff(2L)));

        storeAdminAccessService.requireStoreAccess(managerAuth(10L), 2L);
    }

    @Test
    void managerCannotAccessOtherStore() {
        when(staffRepository.findByUser_UserIdAndStatus(10L, Staff.StaffStatus.ACTIVE))
                .thenReturn(Optional.of(staff(2L)));

        assertThatThrownBy(() -> storeAdminAccessService.requireStoreAccess(managerAuth(10L), 1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("只能管理自己所屬門市");
    }

    @Test
    void managerCannotPerformAdminOnlyOperation() {
        assertThatThrownBy(() -> storeAdminAccessService.requireAdmin(managerAuth(10L)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("僅限系統管理員");
    }

    @Test
    void managerWithoutActiveStaffStoreIsRejected() {
        when(staffRepository.findByUser_UserIdAndStatus(10L, Staff.StaffStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> storeAdminAccessService.requireStoreAccess(managerAuth(10L), 1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("找不到在職員工門市資料");
    }

    private static Authentication adminAuth() {
        return auth(1L, "ROLE_ADMIN");
    }

    private static Authentication managerAuth(Long userId) {
        return auth(userId, "ROLE_MANAGER");
    }

    private static Authentication auth(Long userId, String authority) {
        return new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of(new SimpleGrantedAuthority(authority))
        );
    }

    private static Staff staff(Long storeId) {
        return Staff.builder()
                .store(Store.builder().storeId(storeId).build())
                .status(Staff.StaffStatus.ACTIVE)
                .build();
    }

    private static StoreListResponse storeResponse(Long storeId) {
        return StoreListResponse.builder()
                .storeId(storeId)
                .storeName("敘日門市 " + storeId)
                .build();
    }
}
