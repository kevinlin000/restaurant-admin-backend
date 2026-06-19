package com.restaurant.reservation.repository;

import com.restaurant.reservation.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    // 訂位日曆查詢：只開放後台設定的日期區間時段
    List<TimeSlot> findByStoreIdAndReservationDateBetweenAndIsOpenTrueOrderByReservationDateAscStartTimeAsc(
            Long storeId,
            LocalDate startDate,
            LocalDate endDate
    );

    // 訂位日期時段頁面：查一天的所有時段
    List<TimeSlot> findByStoreIdAndReservationDateOrderByStartTimeAsc(Long storeId, LocalDate reservationDate);

    // 訂位日期時段頁面：查分店全部時段
    List<TimeSlot> findByStoreIdOrderByReservationDateAscStartTimeAsc(Long storeId);

    // 新增、修改時段時檢查是否重複
    Optional<TimeSlot> findByStoreIdAndReservationDateAndStartTime(Long storeId, LocalDate reservationDate, java.time.LocalTime startTime);
}
