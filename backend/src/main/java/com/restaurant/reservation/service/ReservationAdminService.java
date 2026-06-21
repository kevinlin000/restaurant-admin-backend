package com.restaurant.reservation.service;

import com.restaurant.common.BusinessException;
import com.restaurant.common.ResourceNotFoundException;
import com.restaurant.reservation.dto.AssignTableRequest;
import com.restaurant.reservation.dto.CreateReservationRequest;
import com.restaurant.reservation.dto.DailyOverviewResponse;
import com.restaurant.reservation.dto.ReservationResponse;
import com.restaurant.reservation.entity.Reservation;
import com.restaurant.reservation.entity.ReservationTable;
import com.restaurant.reservation.repository.ReservationRepository;
import com.restaurant.reservation.repository.ReservationTableRepository;
import com.restaurant.store.entity.TableInfo;
import com.restaurant.store.repository.TableInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationAdminService {

    private final ReservationRepository reservationRepository;
    private final ReservationTableRepository reservationTableRepository;
    private final TableInfoRepository tableInfoRepository;
    private final ReservationService reservationService;

    public Long getReservationStoreId(Long reservationId) {
        return findReservation(reservationId).getStoreId();
    }

    // 訂位名單頁：查某分店全部訂位 -> 轉成包含時段、桌號的 DTO
    @Transactional
    public List<ReservationResponse> getReservationList(Long storeId) {
        syncNoShowReservations(storeId);
        return reservationRepository.findByStoreIdOrderByCreatedAtDesc(storeId).stream()
                .map(reservationService::toResponse)
                .toList();
    }

    // 查詢分配桌位頁面名單
    @Transactional
    public List<ReservationResponse> getUnassignedReservations(Long storeId, LocalDate date, String status) {
        syncNoShowReservations(storeId);
        String normalizedStatus = (status == null || status.isBlank() || "ALL".equals(status)) ? null : status;
        List<Reservation> reservations = date == null
                ? reservationRepository.findUnassignedByStoreId(storeId, normalizedStatus)
                : reservationRepository.findUnassignedByStoreIdAndReservationDate(storeId, date, normalizedStatus);
        return reservations.stream()
                .map(reservationService::toResponse)
                .toList();
    }

    // 訂位總覽頁：查單日訂位、今日統計
    @Transactional
    public DailyOverviewResponse getDailyOverview(Long storeId, LocalDate date) {
        syncNoShowReservations(storeId);
        List<ReservationResponse> reservations = reservationRepository.findByStoreIdAndReservationDate(storeId, date).stream()
                .map(reservationService::toResponse)
                .toList();

        long total = reservations.stream().filter(this::isCountableReservation).count();
        long checkedIn = reservations.stream().filter(item -> "CHECKED_IN".equals(item.getStatus())).count();
        long assigned = reservations.stream().filter(item -> "ASSIGNED".equals(item.getStatus())).count();
        long unassigned = reservations.stream()
                .filter(item -> ("PENDING".equals(item.getStatus()) || "RESERVED".equals(item.getStatus()))
                        && (item.getTableIds() == null || item.getTableIds().isEmpty()))
                .count();

        return DailyOverviewResponse.builder()
                .storeId(storeId)
                .date(date.toString())
                .totalCount(total)
                .assignedCount(assigned)
                .unassignedCount(unassigned)
                .checkedInCount(checkedIn)
                .reservations(reservations)
                .build();
    }

    // 分配或重新分配桌位
    @Transactional
    public ReservationResponse assignTables(AssignTableRequest request) {
        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new ResourceNotFoundException("訂位", request.getReservationId()));
        syncNoShowReservations(reservation.getStoreId());
        reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new ResourceNotFoundException("訂位", request.getReservationId()));
        if (reservation.isLocked()) {
            throw new BusinessException("已入座或已完成的訂位不能更改桌位");
        }
        if ("CANCELLED".equals(reservation.getStatus())) {
            throw new BusinessException("已取消的訂位不能分配桌位");
        }
        if (!"PENDING".equals(reservation.getStatus()) && !"RESERVED".equals(reservation.getStatus()) && !"ASSIGNED".equals(reservation.getStatus())) {
            throw new BusinessException("此狀態不能分配桌位");
        }
        if (request.getTableIds() == null || request.getTableIds().isEmpty()) {
            throw new BusinessException("至少需要選擇一張桌位");
        }
        Long reservationStoreId = reservation.getStoreId();
        Long reservationId = reservation.getReservationId();

        List<TableInfo> tables = tableInfoRepository.findAllById(request.getTableIds());
        if (tables.size() != request.getTableIds().size()) {
            throw new BusinessException("部分桌位不存在");
        }
        boolean hasOtherStoreTable = tables.stream().anyMatch(table -> !reservationStoreId.equals(table.getStoreId()));
        if (hasOtherStoreTable) {
            throw new BusinessException("不能分配其他分店的桌位");
        }
        validateTablesNotUsedInSameSlot(reservation, request.getTableIds());

        reservationTableRepository.deleteByReservationId(reservationId);
        List<ReservationTable> assignments = request.getTableIds().stream()
                .map(tableId -> ReservationTable.builder()
                        .reservationId(reservationId)
                        .tableId(tableId)
                        .build())
                .toList();
        reservationTableRepository.saveAll(assignments);

        reservation.setStatus("ASSIGNED");
        reservationRepository.save(reservation);
        return reservationService.toResponse(reservation);
    }

    // 同一個 slot，桌位不能被其他訂位重複使用
    private void validateTablesNotUsedInSameSlot(Reservation reservation, List<Long> tableIds) {
        List<Long> sameSlotReservationIds = reservationRepository
                .findBySlotIdAndReservationIdNot(reservation.getSlotId(), reservation.getReservationId())
                .stream()
                .filter(item -> !"CANCELLED".equals(item.getStatus()) && !"NO_SHOW".equals(item.getStatus()))
                .map(Reservation::getReservationId)
                .toList();
        if (sameSlotReservationIds.isEmpty()) {
            return;
        }

        Set<Long> usedTableIds = reservationTableRepository.findByReservationIdIn(sameSlotReservationIds)
                .stream()
                .map(ReservationTable::getTableId)
                .collect(Collectors.toSet());
        boolean hasUsedTable = tableIds.stream().anyMatch(usedTableIds::contains);
        if (hasUsedTable) {
            throw new BusinessException("此日期時段已有訂位使用相同桌位，請重新選擇");
        }
    }

    // 店家後台直接更改保留訂位：PENDING 改成 RESERVED
    @Transactional
    public ReservationResponse reserve(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("訂位", reservationId));
        syncNoShowReservations(reservation.getStoreId());
        reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("訂位", reservationId));
        if (reservation.isLocked()) {
            throw new BusinessException("已入座或已完成的訂位不能更改狀態");
        }
        if ("CANCELLED".equals(reservation.getStatus())) {
            throw new BusinessException("已取消的訂位不能確認保留");
        }
        if ("NO_SHOW".equals(reservation.getStatus())) {
            throw new BusinessException("已逾時未到的訂位不能確認保留");
        }
        reservation.setStatus("RESERVED");
        reservationRepository.save(reservation);
        return reservationService.toResponse(reservation);
    }

    // 訂位總覽勾選入座
    @Transactional
    public ReservationResponse checkIn(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("訂位", reservationId));
        syncNoShowReservations(reservation.getStoreId());
        reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("訂位", reservationId));
        if ("CANCELLED".equals(reservation.getStatus())) {
            throw new BusinessException("已取消的訂位不能入座");
        }
        if ("NO_SHOW".equals(reservation.getStatus())) {
            throw new BusinessException("已逾時未到的訂位不能入座");
        }
        reservation.setStatus("CHECKED_IN");
        reservationRepository.save(reservation);
        return reservationService.toResponse(reservation);
    }

    // 編輯訂位基本資料
    @Transactional
    public ReservationResponse updateReservationInfo(Long reservationId, CreateReservationRequest request) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("訂位", reservationId));
        syncNoShowReservations(reservation.getStoreId());
        reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("訂位", reservationId));
        if (reservation.isLocked() || "CANCELLED".equals(reservation.getStatus())) {
            throw new BusinessException("已入座、已完成或已取消的訂位不能編輯");
        }
        if (!"PENDING".equals(reservation.getStatus()) && !"RESERVED".equals(reservation.getStatus()) && !"ASSIGNED".equals(reservation.getStatus())) {
            throw new BusinessException("此狀態不能編輯訂位資料");
        }

        reservation.setCustomerName(request.getCustomerName());
        reservation.setCustomerPhone(request.getCustomerPhone());
        reservation.setCustomerEmail(request.getCustomerEmail());
        reservation.setPartySize(request.getPartySize());
        reservation.setSpecialRequest(request.getSpecialRequest());
        reservationRepository.save(reservation);
        return reservationService.toResponse(reservation);
    }

    private void syncNoShowReservations(Long storeId) {
        reservationRepository.markNoShowReservations(storeId, LocalDate.now(), LocalTime.now());
    }

    private Reservation findReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("訂位", reservationId));
    }

    private boolean isCountableReservation(ReservationResponse item) {
        return !"CANCELLED".equals(item.getStatus())
                && !"NO_SHOW".equals(item.getStatus())
                && !"CHECKED_IN".equals(item.getStatus())
                && !"COMPLETED".equals(item.getStatus());
    }
}
