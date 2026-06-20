package com.restaurant.reservation.service;

import com.restaurant.common.BusinessException;
import com.restaurant.common.ResourceNotFoundException;
import com.restaurant.reservation.dto.TimeSlotRequest;
import com.restaurant.reservation.entity.ReservationCapacity;
import com.restaurant.reservation.entity.TimeSlot;
import com.restaurant.reservation.repository.ReservationCapacityRepository;
import com.restaurant.reservation.repository.ReservationRepository;
import com.restaurant.reservation.repository.TimeSlotRepository;
import com.restaurant.store.entity.TableInfo;
import com.restaurant.store.repository.TableInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationSettingService {

    private final TableInfoRepository tableInfoRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ReservationCapacityRepository capacityRepository;
    private final ReservationRepository reservationRepository;

    public Long getTimeSlotStoreId(Long slotId) {
        return findTimeSlot(slotId).getStoreId();
    }

    // 日期時段設定名單：查分店全部時段
    public List<TimeSlot> getTimeSlots(Long storeId, java.time.LocalDate date) {
        if (date == null) {
            return timeSlotRepository.findByStoreIdOrderByReservationDateAscStartTimeAsc(storeId);
        }
        return timeSlotRepository.findByStoreIdAndReservationDateOrderByStartTimeAsc(storeId, date);
    }

    // 新增訂位日期時段
    @Transactional
    public TimeSlot createTimeSlot(TimeSlotRequest request) {
        timeSlotRepository.findByStoreIdAndReservationDateAndStartTime(
                request.getStoreId(),
                request.getReservationDate(),
                request.getStartTime()
        ).ifPresent(slot -> {
            throw new BusinessException("此分店、日期、開始時間已存在");
        });

        TimeSlot slot = timeSlotRepository.save(TimeSlot.builder()
                .storeId(request.getStoreId())
                .reservationDate(request.getReservationDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isOpen(request.getIsOpen() == null || request.getIsOpen())
                .build());
        rebuildCapacity(slot.getSlotId());
        return slot;
    }

    // 修改訂位時段
    @Transactional
    public TimeSlot updateTimeSlot(Long slotId, TimeSlotRequest request) {
        TimeSlot slot = findTimeSlot(slotId);

        timeSlotRepository.findByStoreIdAndReservationDateAndStartTime(
                request.getStoreId(),
                request.getReservationDate(),
                request.getStartTime()
        ).ifPresent(existing -> {
            if (!existing.getSlotId().equals(slotId)) {
                throw new BusinessException("此分店、日期、開始時間已存在");
            }
        });

        boolean storeChanged = !slot.getStoreId().equals(request.getStoreId());
        slot.setStoreId(request.getStoreId());
        slot.setReservationDate(request.getReservationDate());
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setIsOpen(request.getIsOpen() == null || request.getIsOpen());

        TimeSlot saved = timeSlotRepository.save(slot);
        if (storeChanged) {
            rebuildCapacity(saved.getSlotId());
        }
        return saved;
    }

    // 刪除時段（已有訂位的時段不能刪）
    @Transactional
    public void deleteTimeSlot(Long slotId) {
        TimeSlot slot = findTimeSlot(slotId);
        if (reservationRepository.existsBySlotId(slotId)) {
            throw new BusinessException("此時段已有訂位，不能刪除");
        }
        capacityRepository.deleteBySlotId(slot.getSlotId());
        timeSlotRepository.delete(slot);
    }

    // 算容量：依 table_info 的可用桌位數建容量 -> reservation_capacity 計算已訂數，算容量
    @Transactional
    public void rebuildCapacity(Long slotId) {
        TimeSlot slot = findTimeSlot(slotId);

        Map<Integer, Integer> reservedCountBySize = capacityRepository.findBySlotIdOrderByTableSizeAsc(slotId)
                .stream()
                .collect(Collectors.toMap(
                        ReservationCapacity::getTableSize,
                        ReservationCapacity::getReservedCount,
                        Integer::sum
                ));

        Map<Integer, Long> countsBySize = tableInfoRepository
                .findByStoreIdAndStatus(slot.getStoreId(), "AVAILABLE")
                .stream()
                .collect(Collectors.groupingBy(TableInfo::getTableSize, Collectors.counting()));

        capacityRepository.deleteBySlotId(slotId);
        List<ReservationCapacity> capacities = countsBySize.entrySet().stream()
                .map(entry -> {
                    int totalCount = entry.getValue().intValue();
                    int reservedCount = Math.min(reservedCountBySize.getOrDefault(entry.getKey(), 0), totalCount);
                    return ReservationCapacity.builder()
                            .slotId(slotId)
                            .tableSize(entry.getKey())
                            .totalCount(totalCount)
                            .reservedCount(reservedCount)
                            .build();
                })
                .toList();
        capacityRepository.saveAll(capacities);
    }

    // 查時段容量，顯示總數、已訂、剩餘。
    public List<ReservationCapacity> getCapacity(Long slotId) {
        return capacityRepository.findBySlotIdOrderByTableSizeAsc(slotId);
    }

    private TimeSlot findTimeSlot(Long slotId) {
        return timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("訂位時段", slotId));
    }
}
