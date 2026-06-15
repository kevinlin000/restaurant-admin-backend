package com.restaurant.store.repository;

import com.restaurant.store.entity.StoreHour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreHourRepository extends JpaRepository<StoreHour, Long> {
    List<StoreHour> findByStoreIdOrderByDayOfWeekAscMealPeriodAscOpenTimeAsc(Long storeId);
    List<StoreHour> findByStoreIdAndDayOfWeekAndIsClosedFalseOrderByOpenTimeAsc(Long storeId, Integer dayOfWeek);
    Optional<StoreHour> findByHourIdAndStoreId(Long hourId, Long storeId);
    Optional<StoreHour> findByStoreIdAndDayOfWeekAndMealPeriod(Long storeId, Integer dayOfWeek, String mealPeriod);
}
