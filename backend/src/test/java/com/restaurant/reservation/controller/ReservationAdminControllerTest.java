package com.restaurant.reservation.controller;

import com.restaurant.reservation.dto.AssignTableRequest;
import com.restaurant.reservation.dto.ReservationResponse;
import com.restaurant.reservation.service.ReservationAdminService;
import com.restaurant.store.service.StoreAdminAccessService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationAdminControllerTest {

    @Mock
    private ReservationAdminService reservationAdminService;

    @Mock
    private StoreAdminAccessService storeAdminAccessService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReservationAdminController reservationAdminController;

    @Test
    void listRequiresAccessToRequestedStore() {
        when(reservationAdminService.getReservationList(2L)).thenReturn(List.of());

        reservationAdminController.getReservationList(2L, authentication);

        verify(storeAdminAccessService).requireStoreAccess(authentication, 2L);
        verify(reservationAdminService).getReservationList(2L);
    }

    @Test
    void assignTablesRequiresAccessToReservationStore() {
        AssignTableRequest request = new AssignTableRequest();
        request.setReservationId(30L);
        request.setTableIds(List.of(9L));
        when(reservationAdminService.getReservationStoreId(30L)).thenReturn(2L);
        when(reservationAdminService.assignTables(request)).thenReturn(ReservationResponse.builder().build());

        reservationAdminController.assignTables(request, authentication);

        verify(storeAdminAccessService).requireStoreAccess(authentication, 2L);
        verify(reservationAdminService).assignTables(request);
    }

    @Test
    void checkInRequiresAccessToReservationStore() {
        when(reservationAdminService.getReservationStoreId(30L)).thenReturn(2L);
        when(reservationAdminService.checkIn(30L)).thenReturn(ReservationResponse.builder().build());

        reservationAdminController.checkIn(30L, authentication);

        verify(storeAdminAccessService).requireStoreAccess(authentication, 2L);
        verify(reservationAdminService).checkIn(30L);
    }
}
