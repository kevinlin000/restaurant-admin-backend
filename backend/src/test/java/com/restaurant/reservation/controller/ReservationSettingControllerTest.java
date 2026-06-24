package com.restaurant.reservation.controller;

import com.restaurant.reservation.dto.TimeSlotRequest;
import com.restaurant.reservation.entity.TimeSlot;
import com.restaurant.reservation.service.ReservationAdminAccessService;
import com.restaurant.reservation.service.ReservationSettingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationSettingControllerTest {

    @Mock
    private ReservationSettingService reservationSettingService;

    @Mock
    private ReservationAdminAccessService reservationAdminAccessService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReservationSettingController reservationSettingController;

    @Test
    void listTimeSlotsRequiresAccessToRequestedStore() {
        LocalDate date = LocalDate.of(2026, 6, 19);
        when(reservationSettingService.getTimeSlots(2L, date)).thenReturn(List.of());

        reservationSettingController.getTimeSlots(2L, date, authentication);

        verify(reservationAdminAccessService).requireStoreAccess(authentication, 2L);
        verify(reservationSettingService).getTimeSlots(2L, date);
    }

    @Test
    void updateTimeSlotRequiresAccessToExistingAndRequestedStore() {
        TimeSlotRequest request = timeSlotRequest(3L);
        when(reservationSettingService.getTimeSlotStoreId(50L)).thenReturn(2L);
        when(reservationSettingService.updateTimeSlot(50L, request)).thenReturn(TimeSlot.builder().build());

        reservationSettingController.updateTimeSlot(50L, request, authentication);

        verify(reservationAdminAccessService).requireManagerOrAdmin(authentication);
        verify(reservationAdminAccessService).requireStoreAccess(authentication, 2L);
        verify(reservationAdminAccessService).requireStoreAccess(authentication, 3L);
        verify(reservationSettingService).updateTimeSlot(50L, request);
    }

    @Test
    void capacityRequiresAccessToSlotStore() {
        when(reservationSettingService.getTimeSlotStoreId(50L)).thenReturn(2L);
        when(reservationSettingService.getCapacity(50L)).thenReturn(List.of());

        reservationSettingController.getCapacity(50L, authentication);

        verify(reservationAdminAccessService).requireStoreAccess(authentication, 2L);
        verify(reservationSettingService).getCapacity(50L);
    }

    private static TimeSlotRequest timeSlotRequest(Long storeId) {
        TimeSlotRequest request = new TimeSlotRequest();
        request.setStoreId(storeId);
        request.setReservationDate(LocalDate.of(2026, 6, 19));
        request.setStartTime(LocalTime.of(18, 0));
        request.setEndTime(LocalTime.of(20, 0));
        request.setIsOpen(true);
        return request;
    }
}
