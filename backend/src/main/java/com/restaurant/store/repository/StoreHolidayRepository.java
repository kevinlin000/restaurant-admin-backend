package com.restaurant.store.repository;

import com.restaurant.store.entity.StoreHoliday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StoreHolidayRepository extends JpaRepository<StoreHoliday, Long> {
    List<StoreHoliday> findByStoreId(Long storeId);
    Optional<StoreHoliday> findByStoreIdAndHolidayDate(Long storeId, LocalDate date);
    List<StoreHoliday> findByStoreIdInAndHolidayDate(List<Long> storeIds, LocalDate date);
    List<StoreHoliday> findByStoreIdAndHolidayDateBetweenOrderByHolidayDateAsc(Long storeId, LocalDate from, LocalDate to);
    List<StoreHoliday> findByStoreIdOrderByHolidayDateAsc(Long storeId);
    Optional<StoreHoliday> findByHolidayIdAndStoreId(Long holidayId, Long storeId);
}
