package com.restaurant.reservation.repository;

import com.restaurant.reservation.entity.ReservationCapacity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReservationCapacityRepository extends JpaRepository<ReservationCapacity, Long> {

    // 顯示時段容量列表，依桌型大小排序
    List<ReservationCapacity> findBySlotIdOrderByTableSizeAsc(Long slotId);

    // 依人數找「最小但可容納」的桌型容量 ex: 3 人先找 4 人桌
    Optional<ReservationCapacity> findFirstBySlotIdAndTableSizeGreaterThanEqualOrderByTableSizeAsc(Long slotId, Integer partySize);

    // ＊建立/修改/取消訂位時，避免多人同時訂位造成 reserved_count 超賣＊
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select rc from ReservationCapacity rc where rc.capacityId = :capacityId")
    Optional<ReservationCapacity> findByCapacityIdForUpdate(Long capacityId);

    // ＊保留 slot + 桌型精準查容量的情境＊
    Optional<ReservationCapacity> findBySlotIdAndTableSize(Long slotId, Integer tableSize);

    // 重算容量、刪除時段時 -> 清掉 slot 的 capacity
    void deleteBySlotId(Long slotId);
}
