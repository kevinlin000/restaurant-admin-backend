package com.restaurant.reservation.repository;

import com.restaurant.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 刪除時段前，檢查是否已有訂位（避免 time_slot 被刪 reservation 失去關聯）
    boolean existsBySlotId(Long slotId);

    // 訂位名單頁 -> 依分店查全部訂位，最新建立的排前面
    List<Reservation> findByStoreIdOrderByCreatedAtDesc(Long storeId);

    // 後台查詢前先把已逾時且未入座的訂位改成 NO_SHOW。
    @Modifying
    @Query("""
            update Reservation r
            set r.status = 'NO_SHOW'
            where r.storeId = :storeId
              and r.status in ('PENDING', 'RESERVED', 'ASSIGNED')
              and exists (
                select 1
                from TimeSlot ts
                where ts.slotId = r.slotId
                  and (
                    ts.reservationDate < :today
                    or (ts.reservationDate = :today and ts.endTime < :now)
                  )
              )
            """)
    int markNoShowReservations(Long storeId, LocalDate today, LocalTime now);

    // 依狀態查訂位
    List<Reservation> findByStoreIdAndStatusOrderByCreatedAtAsc(Long storeId, String status);

    // 顧客端查詢自己的訂位，最新建立的排前面
    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 信件連結不依賴會員登入 JWT，改用訂位本身的 access_token 驗證
    Optional<Reservation> findByReservationIdAndAccessToken(Long reservationId, String accessToken);

    boolean existsByAccessToken(String accessToken);

    // 分配桌位時檢查同一時段其他訂位，避免同一桌被重複分配
    List<Reservation> findBySlotIdAndReservationIdNot(Long slotId, Long reservationId);

    // ＊訂位總覽查分店某一天訂位＊
    @Query("""
            select r
            from Reservation r
            join TimeSlot ts on ts.slotId = r.slotId
            where r.storeId = :storeId
              and ts.reservationDate = :reservationDate
            order by ts.startTime asc, r.createdAt asc
            """)
    List<Reservation> findByStoreIdAndReservationDate(Long storeId, LocalDate reservationDate);

    // 分配桌位頁：未配桌、已保留 -> 需沒有桌位 reservation_table；已配桌 -> 讓店家可重新配桌。
    @Query("""
            select distinct r
            from Reservation r
            left join ReservationTable rt on rt.reservationId = r.reservationId
            where r.storeId = :storeId
              and (:status is null or r.status = :status)
              and (
                (r.status in ('PENDING', 'RESERVED') and rt.id is null)
                or r.status = 'ASSIGNED'
              )
            order by r.createdAt asc
            """)
    List<Reservation> findUnassignedByStoreId(Long storeId, String status);

    // 分配桌位頁加日期篩選，保留 ASSIGNED 以支援重新配桌
    @Query("""
            select distinct r
            from Reservation r
            join TimeSlot ts on ts.slotId = r.slotId
            left join ReservationTable rt on rt.reservationId = r.reservationId
            where r.storeId = :storeId
              and ts.reservationDate = :reservationDate
              and (:status is null or r.status = :status)
              and (
                (r.status in ('PENDING', 'RESERVED') and rt.id is null)
                or r.status = 'ASSIGNED'
              )
            order by ts.startTime asc, r.createdAt asc
            """)
    List<Reservation> findUnassignedByStoreIdAndReservationDate(Long storeId, LocalDate reservationDate, String status);
}
