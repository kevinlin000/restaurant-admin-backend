package com.restaurant.store.controller;

import com.restaurant.store.dto.request.StoreStatusUpdateRequest;
import com.restaurant.store.dto.response.StoreDetailResponse;
import com.restaurant.store.service.StoreAdminAccessService;
import com.restaurant.store.service.StoreService;
import com.restaurant.store.service.StoreStatusService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreAdminControllerTest {

    @Mock
    private StoreService storeService;

    @Mock
    private StoreAdminAccessService storeAdminAccessService;

    @Mock
    private StoreStatusService storeStatusService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private StoreAdminController storeAdminController;

    @Test
    void updateStoreStatusRequiresAccessToStore() {
        StoreStatusUpdateRequest request = statusRequest("CLOSED");
        StoreDetailResponse response = StoreDetailResponse.builder().storeId(2L).status("CLOSED").build();
        when(storeService.getStoreDetailForAdmin(2L)).thenReturn(response);

        storeAdminController.updateStoreStatus(2L, request, authentication);

        verify(storeAdminAccessService).requireStoreAccess(authentication, 2L);
        verify(storeStatusService).updateStoreStatus(2L, "CLOSED");
        verify(storeService).getStoreDetailForAdmin(2L);
    }

    @Test
    void updateGlobalStoreStatusRequiresAdmin() {
        StoreStatusUpdateRequest request = statusRequest("PAUSED");
        when(storeStatusService.updateGlobalStatus("PAUSED")).thenReturn(3);

        storeAdminController.updateGlobalStoreStatus(request, authentication);

        verify(storeAdminAccessService).requireAdmin(authentication);
        verify(storeStatusService).updateGlobalStatus("PAUSED");
    }

    private static StoreStatusUpdateRequest statusRequest(String status) {
        StoreStatusUpdateRequest request = new StoreStatusUpdateRequest();
        request.setStatus(status);
        return request;
    }
}
