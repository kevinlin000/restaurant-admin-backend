package com.restaurant.store.repository;

import com.restaurant.store.entity.StoreHour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreHourRepository extends JpaRepository<StoreHour, Long> {
    List<StoreHour> findByStoreId(Long storeId);
    List<StoreHour> findByStoreIdAndDayOfWeekAndIsClosedFalse(Long storeId, Integer dayOfWeek);
}
