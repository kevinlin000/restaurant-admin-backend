package com.restaurant.store.service;

import com.restaurant.common.BusinessException;
import com.restaurant.common.ResourceNotFoundException;
import com.restaurant.store.entity.Store;
import com.restaurant.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StoreStatusService {

    private static final Set<String> VALID_STATUSES = Set.of("PREPARING", "OPEN", "PAUSED", "CLOSED");

    private final StoreRepository storeRepository;

    @Transactional
    @CacheEvict(value = "store_menus", key = "#storeId")
    public void updateStoreStatus(Long storeId, String status) {
        String normalizedStatus = normalizeStatus(status);
        Store store = storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("門市", storeId));
        store.setStatus(normalizedStatus);
        storeRepository.save(store);
    }

    @Transactional
    @CacheEvict(value = "store_menus", allEntries = true)
    public int updateGlobalStatus(String status) {
        String normalizedStatus = normalizeStatus(status);
        List<Store> stores = storeRepository.findByIsDeletedFalseOrderByCityAscDistrictAscStoreNameAsc();
        stores.forEach(store -> store.setStatus(normalizedStatus));
        storeRepository.saveAll(stores);
        return stores.size();
    }

    @Transactional(readOnly = true)
    public Boolean checkStoreOpen(Long storeId) {
        return storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .map(store -> "OPEN".equals(store.getStatus()))
                .orElse(false);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new BusinessException("狀態不可為空");
        }

        String normalizedStatus = status.trim().toUpperCase(Locale.ROOT);
        if (!VALID_STATUSES.contains(normalizedStatus)) {
            throw new BusinessException("狀態值不合法：" + status);
        }
        return normalizedStatus;
    }
}
