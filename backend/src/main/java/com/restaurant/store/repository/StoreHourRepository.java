package com.restaurant.store.repository;

import com.restaurant.store.entity.StoreHour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreHourRepository extends JpaRepository<StoreHour, Long> {
    List<StoreHour> findByStoreIdOrderByDayOfWeekAscMealPeriodAscOpenTimeAsc(Long storeId);
    List<StoreHour> findByStoreIdAndDayOfWeekAndIsClosedFalseOrderByOpenTimeAsc(Long storeId, Integer dayOfWeek);
}
