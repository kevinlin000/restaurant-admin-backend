package com.restaurant.store.service;

import com.restaurant.common.BusinessException;
import com.restaurant.common.ResourceNotFoundException;
import com.restaurant.store.entity.Store;
import com.restaurant.store.repository.StoreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreStatusServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private StoreStatusService storeStatusService;

    @Test
    void updateStoreStatusNormalizesAndPersistsStatus() {
        Store store = store(1L, "OPEN");
        when(storeRepository.findByStoreIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(store));

        storeStatusService.updateStoreStatus(1L, " closed ");

        assertThat(store.getStatus()).isEqualTo("CLOSED");
        verify(storeRepository).save(store);
    }

    @Test
    void updateStoreStatusRejectsUnsupportedStatus() {
        assertThatThrownBy(() -> storeStatusService.updateStoreStatus(1L, "HOLIDAY"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("狀態值不合法");

        verify(storeRepository, never()).save(any(Store.class));
    }

    @Test
    void updateStoreStatusRejectsMissingStore() {
        when(storeRepository.findByStoreIdAndIsDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storeStatusService.updateStoreStatus(99L, "OPEN"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateGlobalStatusUpdatesAllActiveStores() {
        Store first = store(1L, "OPEN");
        Store second = store(2L, "OPEN");
        when(storeRepository.findByIsDeletedFalseOrderByCityAscDistrictAscStoreNameAsc())
                .thenReturn(List.of(first, second));

        int updatedCount = storeStatusService.updateGlobalStatus("PAUSED");

        assertThat(updatedCount).isEqualTo(2);
        assertThat(first.getStatus()).isEqualTo("PAUSED");
        assertThat(second.getStatus()).isEqualTo("PAUSED");
        verify(storeRepository).saveAll(List.of(first, second));
    }

    @Test
    void checkStoreOpenReturnsTrueOnlyForOpenStore() {
        when(storeRepository.findByStoreIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(store(1L, "OPEN")));
        when(storeRepository.findByStoreIdAndIsDeletedFalse(2L)).thenReturn(Optional.of(store(2L, "PAUSED")));
        when(storeRepository.findByStoreIdAndIsDeletedFalse(3L)).thenReturn(Optional.empty());

        assertThat(storeStatusService.checkStoreOpen(1L)).isTrue();
        assertThat(storeStatusService.checkStoreOpen(2L)).isFalse();
        assertThat(storeStatusService.checkStoreOpen(3L)).isFalse();
    }

    private static Store store(Long storeId, String status) {
        return Store.builder()
                .storeId(storeId)
                .brandId(1L)
                .storeCode("S" + storeId)
                .storeName("測試門市 " + storeId)
                .city("台北市")
                .district("信義區")
                .address("測試路 1 號")
                .status(status)
                .isDeleted(false)
                .build();
    }
}
