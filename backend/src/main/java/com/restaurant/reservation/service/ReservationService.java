package com.restaurant.reservation.service;

import com.restaurant.common.BusinessException;
import com.restaurant.common.ResourceNotFoundException;
import com.restaurant.reservation.dto.CreateReservationRequest;
import com.restaurant.reservation.dto.ReservationResponse;
import com.restaurant.reservation.entity.Reservation;
import com.restaurant.reservation.entity.ReservationCapacity;
import com.restaurant.reservation.entity.ReservationTable;
import com.restaurant.reservation.entity.TimeSlot;
import com.restaurant.reservation.repository.ReservationCapacityRepository;
import com.restaurant.reservation.repository.ReservationRepository;
import com.restaurant.reservation.repository.ReservationTableRepository;
import com.restaurant.reservation.repository.TimeSlotRepository;
// import com.restaurant.store.entity.Store;
import com.restaurant.store.entity.StoreHour;
import com.restaurant.store.entity.TableInfo;
import com.restaurant.store.repository.StoreHolidayRepository;
import com.restaurant.store.repository.StoreHourRepository;
// import com.restaurant.store.repository.StoreRepository;
import com.restaurant.store.repository.TableInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ReservationCapacityRepository capacityRepository;
    private final ReservationTableRepository reservationTableRepository;
    private final TableInfoRepository tableInfoRepository;
    // private final StoreRepository storeRepository;
    private final StoreHolidayRepository storeHolidayRepository;
    private final StoreHourRepository storeHourRepository;
    private final ReservationEmailService reservationEmailService;

    // 顧客端查可訂時段
    public List<TimeSlot> getAvailableSlots(Long storeId, LocalDate startDate, LocalDate endDate) {
        // Store store = storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
        //         .orElseThrow(() -> new ResourceNotFoundException("分店", storeId));
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(30);
        LocalDate effectiveStart = startDate.isBefore(today) ? today : startDate;
        LocalDate effectiveEnd = endDate.isAfter(maxDate) ? maxDate : endDate;
        List<LocalDate> holidays = storeHolidayRepository.findByStoreId(storeId).stream()
                .map(holiday -> holiday.getHolidayDate())
                .toList();

        return timeSlotRepository.findByStoreIdAndReservationDateBetweenAndIsOpenTrueOrderByReservationDateAscStartTimeAsc(
                storeId,
                effectiveStart,
                effectiveEnd
        ).stream()
                .filter(slot -> !holidays.contains(slot.getReservationDate()))
                .filter(this::isWithinBusinessHours)
                .toList();
    }

    // 查某時段容量
    public List<ReservationCapacity> getAvailableCapacity(Long slotId) {
        return capacityRepository.findBySlotIdOrderByTableSizeAsc(slotId);
    }

    // 建立訂位
    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request) {
        TimeSlot slot = timeSlotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("訂位時段", request.getSlotId()));
        if (!Boolean.TRUE.equals(slot.getIsOpen())) {
            throw new BusinessException("此時段未開放訂位");
        }
        if (!slot.getStoreId().equals(request.getStoreId())) {
            throw new BusinessException("訂位分店與時段分店不一致");
        }
        validateWithinBusinessHours(slot);
        BigDecimal depositAmount = resolveReservationDepositAmount(slot);
        String paymentStatus = depositAmount.compareTo(BigDecimal.ZERO) > 0 ? "UNPAID" : "NOT_REQUIRED";
        Reservation reservation = createReservationRecord(request, slot, depositAmount, paymentStatus);
        ReservationResponse response = toResponse(reservation);
        if ("UNPAID".equals(paymentStatus)) {
            sendReservationPaymentPendingEmailAfterCommit(response);
        } else {
            sendReservationCreatedEmailAfterCommit(response);
        }
        return response;
    }

    // 需訂金也會先建立 UNPAID 訂位並扣容量，避免付款期間超賣
    private Reservation createReservationRecord(CreateReservationRequest request, TimeSlot slot, BigDecimal depositAmount, String paymentStatus) {
        ReservationCapacity selected = capacityRepository
                .findFirstBySlotIdAndTableSizeGreaterThanEqualOrderByTableSizeAsc(slot.getSlotId(), request.getPartySize())
                .orElseThrow(() -> new BusinessException("此人數目前沒有可訂桌位"));

        ReservationCapacity lockedCapacity = capacityRepository.findByCapacityIdForUpdate(selected.getCapacityId())
                .orElseThrow(() -> new ResourceNotFoundException("訂位容量", selected.getCapacityId()));
        if (!lockedCapacity.hasRemaining()) {
            throw new BusinessException("此時段已額滿");
        }

        lockedCapacity.setReservedCount(lockedCapacity.getReservedCount() + 1);
        capacityRepository.save(lockedCapacity);

        Reservation reservation = reservationRepository.save(Reservation.builder()
                .userId(request.getUserId())
                .storeId(request.getStoreId())
                .slotId(request.getSlotId())
                .partySize(request.getPartySize())
                .status("PENDING")
                .depositAmount(depositAmount == null ? BigDecimal.ZERO : depositAmount)
                .paymentStatus(paymentStatus)
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .customerEmail(request.getCustomerEmail())
                .accessToken(generateReservationAccessToken())
                .specialRequest(request.getSpecialRequest())
                .build());
        return reservation;
    }

    // 依時段設定是否需要訂金；需要訂金時會先建立 UNPAID 訂位，扣除桌位容量
    private BigDecimal resolveReservationDepositAmount(TimeSlot slot) {
        if (!Boolean.TRUE.equals(slot.getRequiresDeposit())) {
            return BigDecimal.ZERO;
        }
        BigDecimal depositAmount = slot.getDepositAmount() == null ? BigDecimal.ZERO : slot.getDepositAmount();
        if (depositAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("訂金金額設定錯誤");
        }
        return depositAmount;
    }

    // 讀取單筆訂位：給訂位成功頁、編輯頁使用
    // 轉成前端需要的 ReservationResponse
    public ReservationResponse getReservation(Long reservationId) {
        return toResponse(reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("訂位", reservationId)));
    }

    // 信件中的成功頁連結使用 reservation_id + access_token 查詢，不需要會員登入 JWT
    public ReservationResponse getReservationByAccessToken(Long reservationId, String accessToken) {
        return toResponse(requireReservationAccess(reservationId, accessToken));
    }

    @Transactional
    public ReservationResponse updateReservationByAccessToken(Long reservationId, String accessToken, CreateReservationRequest request) {
        requireReservationAccess(reservationId, accessToken);
        return updateReservation(reservationId, request);
    }

    @Transactional
    public ReservationResponse reserveReservationByAccessToken(Long reservationId, String accessToken) {
        requireReservationAccess(reservationId, accessToken);
        return reserveReservation(reservationId);
    }

    @Transactional
    public void cancelReservationByAccessToken(Long reservationId, String accessToken) {
        requireReservationAccess(reservationId, accessToken);
        cancelReservation(reservationId);
    }

    // 顧客編輯訂位：只有 PENDING 能改
    @Transactional
    public ReservationResponse updateReservation(Long reservationId, CreateReservationRequest request) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("訂位", reservationId));
        if (!"PENDING".equals(reservation.getStatus())) {
            throw new BusinessException("已保留、已配桌或已入座的訂位不能由顧客端編輯");
        }

        TimeSlot slot = timeSlotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("訂位時段", request.getSlotId()));
        if (!Boolean.TRUE.equals(slot.getIsOpen())) {
            throw new BusinessException("此時段未開放訂位");
        }
        if (!slot.getStoreId().equals(request.getStoreId())) {
            throw new BusinessException("訂位分店與時段分店不一致");
        }
        validateWithinBusinessHours(slot);

        boolean capacityChanged = !reservation.getSlotId().equals(request.getSlotId())
                || !reservation.getPartySize().equals(request.getPartySize());
        if (capacityChanged) {
            releaseCapacity(reservation.getSlotId(), reservation.getPartySize());
            reserveCapacity(request.getSlotId(), request.getPartySize());
        }

        reservation.setUserId(request.getUserId());
        reservation.setStoreId(request.getStoreId());
        reservation.setSlotId(request.getSlotId());
        reservation.setPartySize(request.getPartySize());
        reservation.setCustomerName(request.getCustomerName());
        reservation.setCustomerPhone(request.getCustomerPhone());
        reservation.setCustomerEmail(request.getCustomerEmail());
        reservation.setSpecialRequest(request.getSpecialRequest());
        return toResponse(reservationRepository.save(reservation));
    }

    // 顧客 or 後台確認保留：PENDING 改成 RESERVED
    @Transactional
    public ReservationResponse reserveReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("訂位", reservationId));
        if (reservation.isLocked()) {
            throw new BusinessException("已入座或已完成的訂位不能更改狀態");
        }
        if ("CANCELLED".equals(reservation.getStatus())) {
            throw new BusinessException("已取消的訂位不能保留");
        }
        if ("PENDING".equals(reservation.getStatus())) {
            reservation.setStatus("RESERVED");
            reservationRepository.save(reservation);
        }
        return toResponse(reservation);
    }

    // 取消訂位
    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("訂位", reservationId));
        if (reservation.isLocked()) {
            throw new BusinessException("已入座或已完成的訂位不能取消");
        }
        if ("CANCELLED".equals(reservation.getStatus())) {
            return;
        }

        releaseCapacity(reservation.getSlotId(), reservation.getPartySize());

        reservationTableRepository.deleteByReservationId(reservationId);
        reservation.setStatus("CANCELLED");
        reservationRepository.save(reservation);
    }

    // 訂位扣容量方法
    private void reserveCapacity(Long slotId, Integer partySize) {
        ReservationCapacity selected = capacityRepository
                .findFirstBySlotIdAndTableSizeGreaterThanEqualOrderByTableSizeAsc(slotId, partySize)
                .orElseThrow(() -> new BusinessException("此人數目前沒有可訂桌位"));
        ReservationCapacity lockedCapacity = capacityRepository.findByCapacityIdForUpdate(selected.getCapacityId())
                .orElseThrow(() -> new ResourceNotFoundException("訂位容量", selected.getCapacityId()));
        if (!lockedCapacity.hasRemaining()) {
            throw new BusinessException("此時段已額滿");
        }
        lockedCapacity.setReservedCount(lockedCapacity.getReservedCount() + 1);
        capacityRepository.save(lockedCapacity);
    }

    // 加回容量方法
    private void releaseCapacity(Long slotId, Integer partySize) {
        capacityRepository
                .findFirstBySlotIdAndTableSizeGreaterThanEqualOrderByTableSizeAsc(slotId, partySize)
                .flatMap(capacity -> capacityRepository.findByCapacityIdForUpdate(capacity.getCapacityId()))
                .ifPresent(capacity -> {
                    capacity.setReservedCount(Math.max(0, capacity.getReservedCount() - 1));
                    capacityRepository.save(capacity);
                });
    }

    // ＊每分鐘檢查一次：訂金訂位建立後超過 1 小時仍未付款，就自動取消並釋放容量＊
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void cancelExpiredUnpaidDepositReservations() {
        LocalDateTime deadline = LocalDateTime.now().minusHours(1);
        reservationRepository.findExpiredUnpaidDepositReservations(deadline).forEach(reservation -> {
            cancelExpiredUnpaidDepositReservationNow(reservation.getReservationId());
        });
    }

    // ＊付款頁檢查到逾時時也會呼叫；新交易可避免後續拋錯導致取消被 rollback＊
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cancelExpiredUnpaidDepositReservationNow(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("訂位", reservationId));
        if ("CANCELLED".equals(reservation.getStatus()) || "PAID".equals(reservation.getPaymentStatus())) {
            return;
        }
        releaseCapacity(reservation.getSlotId(), reservation.getPartySize());
        reservationTableRepository.deleteByReservationId(reservation.getReservationId());
        reservation.setStatus("CANCELLED");
        reservationRepository.save(reservation);
    }

    // 分店營業時間判斷
    private boolean isWithinBusinessHours(TimeSlot slot) {
        if (slot == null || slot.getReservationDate() == null || slot.getStartTime() == null || slot.getEndTime() == null) {
            return false;
        }
        Integer dayOfWeek = slot.getReservationDate().getDayOfWeek().getValue();
        List<StoreHour> openHours = storeHourRepository
                .findByStoreIdAndDayOfWeekAndIsClosedFalseOrderByOpenTimeAsc(slot.getStoreId(), dayOfWeek);

        return openHours.stream().anyMatch(hour ->
                hour.getOpenTime() != null
                        && hour.getCloseTime() != null
                        && !slot.getStartTime().isBefore(hour.getOpenTime())
                        && !slot.getEndTime().isAfter(hour.getCloseTime())
        );
    }

    private void validateWithinBusinessHours(TimeSlot slot) {
        if (!isWithinBusinessHours(slot)) {
            throw new BusinessException("此時段不在分店營業時間內");
        }
    }

    // 寄訂位成功 gmail
    private void sendReservationCreatedEmailAfterCommit(ReservationResponse response) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            reservationEmailService.sendReservationCreatedEmail(response);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                reservationEmailService.sendReservationCreatedEmail(response);
            }
        });
    }

    // 寄待付款信，讓顧客一小時內能從信件連結回成功頁繼續付款
    private void sendReservationPaymentPendingEmailAfterCommit(ReservationResponse response) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            reservationEmailService.sendReservationPaymentPendingEmail(response);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                reservationEmailService.sendReservationPaymentPendingEmail(response);
            }
        });
    }

    // AccessToken
    private String generateReservationAccessToken() {
        String token;
        do {
            token = UUID.randomUUID().toString().replace("-", "");
        } while (reservationRepository.existsByAccessToken(token));
        return token;
    }

    private Reservation requireReservationAccess(Long reservationId, String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new BusinessException("訂位連結驗證失敗");
        }

        return reservationRepository
                .findByReservationIdAndAccessToken(reservationId, accessToken)
                .orElseThrow(() -> new BusinessException("訂位連結驗證失敗"));
    }

    // Entity 轉 DTO 補上時段日期、開始結束時間、已配桌桌號 -> 前端不用再分別查多張表
    public ReservationResponse toResponse(Reservation reservation) {
        TimeSlot slot = timeSlotRepository.findById(reservation.getSlotId()).orElse(null);
        List<ReservationTable> assignments = reservationTableRepository.findByReservationId(reservation.getReservationId());
        List<Long> tableIds = assignments.stream().map(ReservationTable::getTableId).toList();
        Map<Long, TableInfo> tableById = tableInfoRepository.findAllById(tableIds).stream()
                .collect(Collectors.toMap(TableInfo::getTableId, Function.identity()));
        List<String> tableNumbers = tableIds.stream()
                .map(tableById::get)
                .filter(table -> table != null)
                .map(TableInfo::getTableNumber)
                .toList();

        return ReservationResponse.builder()
                .reservationId(reservation.getReservationId())
                .userId(reservation.getUserId())
                .storeId(reservation.getStoreId())
                .slotId(reservation.getSlotId())
                .reservationDate(slot == null ? null : slot.getReservationDate())
                .startTime(slot == null ? null : slot.getStartTime())
                .endTime(slot == null ? null : slot.getEndTime())
                .partySize(reservation.getPartySize())
                .status(reservation.getStatus())
                .depositAmount(reservation.getDepositAmount())
                .paymentStatus(reservation.getPaymentStatus())
                .specialRequest(reservation.getSpecialRequest())
                .customerName(reservation.getCustomerName())
                .customerPhone(reservation.getCustomerPhone())
                .customerEmail(reservation.getCustomerEmail())
                .accessToken(reservation.getAccessToken())
                .tableIds(tableIds)
                .tableNumbers(tableNumbers)
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}
