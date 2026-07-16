package com.restaurant.reservation.repository;

import com.restaurant.reservation.entity.ReservationTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationTableRepository extends JpaRepository<ReservationTable, Long> {

    // 查已分配的桌位名單
    List<ReservationTable> findByReservationId(Long reservationId);

    // 檢查同一時段其他訂位已使用的桌位
    List<ReservationTable> findByReservationIdIn(List<Long> reservationIds);

    // ＊保留給檢查桌位是否被使用的＊
    boolean existsByTableId(Long tableId);

    // 取消訂位、重新配桌時 -> 先刪除舊的桌位綁定
    void deleteByReservationId(Long reservationId);
}
