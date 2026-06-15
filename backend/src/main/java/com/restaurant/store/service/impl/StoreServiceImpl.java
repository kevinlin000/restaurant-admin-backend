package com.restaurant.store.service.impl;

import com.restaurant.common.BusinessException;
import com.restaurant.common.ResourceNotFoundException;
import com.restaurant.store.dto.request.*;
import com.restaurant.store.dto.response.*;
import com.restaurant.store.entity.*;
import com.restaurant.store.repository.*;
import com.restaurant.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final StoreHourRepository storeHourRepository;
    private final StoreHolidayRepository storeHolidayRepository;
    private final StoreImageRepository storeImageRepository;
    private final TableInfoRepository tableInfoRepository;

    // =================== 前台 ===================

    @Override
    @Transactional(readOnly = true)
    public List<StoreListResponse> getAllOpenStores() {
        return storeRepository.findByIsDeletedFalseAndStatusOrderByCityAscDistrictAscStoreNameAsc("OPEN")
                .stream()
                .map(s -> toListResponse(s, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreListResponse> searchStores(String keyword) {
        return storeRepository.searchByKeyword(keyword)
                .stream()
                .map(s -> toListResponse(s, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreListResponse> getStoresByCity(String city) {
        return storeRepository.findByIsDeletedFalseAndStatusAndCityOrderByDistrictAscStoreNameAsc("OPEN", city)
                .stream()
                .map(s -> toListResponse(s, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreListResponse> getStoresByCityAndDistrict(String city, String district) {
        return storeRepository.findByIsDeletedFalseAndStatusAndCityAndDistrictOrderByStoreNameAsc("OPEN", city, district)
                .stream()
                .map(s -> toListResponse(s, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreListResponse> findNearbyStores(NearbySearchRequest request) {
        return storeRepository.findByIsDeletedFalseAndStatusOrderByCityAscDistrictAscStoreNameAsc("OPEN")
                .stream()
                .filter(s -> s.getLatitude() != null && s.getLongitude() != null)
                .map(s -> {
                    double dist = haversineKm(
                            request.getLatitude(), request.getLongitude(),
                            s.getLatitude().doubleValue(), s.getLongitude().doubleValue());
                    return toListResponse(s, dist);
                })
                .sorted(Comparator.comparingDouble(StoreListResponse::getDistanceKm))
                .limit(request.getLimit())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StoreDetailResponse getStoreDetail(Long storeId) {
        Store store = storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("門市", storeId));

        List<StoreHour> hours = storeHourRepository.findByStoreIdOrderByDayOfWeekAscMealPeriodAscOpenTimeAsc(storeId);
        List<StoreImage> images = storeImageRepository.findByStoreIdOrderBySortOrderAsc(storeId);
        List<TableInfo> tables = tableInfoRepository.findByStoreIdOrderByZoneAscTableNumberAsc(storeId);
        List<StoreHoliday> holidays = storeHolidayRepository.findByStoreIdAndHolidayDateBetweenOrderByHolidayDateAsc(
                storeId, LocalDate.now(), LocalDate.now().plusDays(30));

        return StoreDetailResponse.builder()
                .storeId(store.getStoreId())
                .storeCode(store.getStoreCode())
                .storeName(store.getStoreName())
                .city(store.getCity())
                .district(store.getDistrict())
                .address(store.getAddress())
                .phone(store.getPhone())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .mainImageUrl(store.getMainImageUrl())
                .status(store.getStatus())
                .mrtInfo(store.getMrtInfo())
                .parkingInfo(store.getParkingInfo())
                .description(store.getDescription())
                .isOpenNow(isOpenNow(storeId))
                .storeHours(hours.stream().map(this::toHourResponse).collect(Collectors.toList()))
                .imageUrls(images.stream().map(StoreImage::getImageUrl).collect(Collectors.toList()))
                .tables(tables.stream().map(this::toTableResponse).collect(Collectors.toList()))
                .upcomingHolidays(holidays.stream().map(this::toHolidayResponse).collect(Collectors.toList()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getCities() {
        return storeRepository.findDistinctCities();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getDistrictsByCity(String city) {
        return storeRepository.findDistinctDistrictsByCity(city);
    }

    // =================== 後台 ===================

    @Override
    @Transactional(readOnly = true)
    public List<StoreListResponse> getAllStoresForAdmin() {
        return storeRepository.findByIsDeletedFalseOrderByCityAscDistrictAscStoreNameAsc()
                .stream()
                .map(s -> toListResponse(s, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StoreDetailResponse createStore(StoreCreateRequest request) {
        Store store = Store.builder()
                .brandId(request.getBrandId())
                .storeCode(request.getStoreCode())
                .storeName(request.getStoreName())
                .city(request.getCity())
                .district(request.getDistrict())
                .address(request.getAddress())
                .phone(request.getPhone())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .mrtInfo(request.getMrtInfo())
                .parkingInfo(request.getParkingInfo())
                .description(request.getDescription())
                .mainImageUrl(request.getMainImageUrl())
                .build();
        store = storeRepository.save(store);
        return getStoreDetail(store.getStoreId());
    }

    @Override
    @Transactional
    public StoreDetailResponse updateStore(Long storeId, StoreUpdateRequest request) {
        Store store = storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("門市", storeId));
        if (request.getStoreName() != null) store.setStoreName(request.getStoreName());
        if (request.getCity() != null) store.setCity(request.getCity());
        if (request.getDistrict() != null) store.setDistrict(request.getDistrict());
        if (request.getAddress() != null) store.setAddress(request.getAddress());
        if (request.getPhone() != null) store.setPhone(request.getPhone());
        if (request.getLatitude() != null) store.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) store.setLongitude(request.getLongitude());
        if (request.getMrtInfo() != null) store.setMrtInfo(request.getMrtInfo());
        if (request.getParkingInfo() != null) store.setParkingInfo(request.getParkingInfo());
        if (request.getDescription() != null) store.setDescription(request.getDescription());
        if (request.getMainImageUrl() != null) store.setMainImageUrl(request.getMainImageUrl());
        if (request.getStatus() != null) store.setStatus(request.getStatus());
        storeRepository.save(store);
        return getStoreDetail(storeId);
    }

    @Override
    @Transactional
    public void deleteStore(Long storeId) {
        Store store = storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("門市", storeId));
        store.setIsDeleted(true);
        storeRepository.save(store);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TableInfoResponse> getTablesByStore(Long storeId) {
        storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("門市", storeId));
        return tableInfoRepository.findByStoreIdOrderByZoneAscTableNumberAsc(storeId)
                .stream().map(this::toTableResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TableInfoResponse createTable(Long storeId, TableCreateRequest request) {
        storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("門市", storeId));
        if (tableInfoRepository.existsByStoreIdAndTableNumber(storeId, request.getTableNumber())) {
            throw new BusinessException("桌號 " + request.getTableNumber() + " 在此門市已存在");
        }
        TableInfo table = TableInfo.builder()
                .storeId(storeId)
                .tableNumber(request.getTableNumber())
                .tableSize(request.getTableSize())
                .tableType(request.getTableType())
                .zone(request.getZone())
                .isCombinable(Boolean.TRUE.equals(request.getIsCombinable()))
                .build();
        return toTableResponse(tableInfoRepository.save(table));
    }

    @Override
    @Transactional
    public TableInfoResponse updateTable(Long storeId, Long tableId, TableUpdateRequest request) {
        storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("門市", storeId));

        TableInfo table = tableInfoRepository.findByTableIdAndStoreId(tableId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("桌位", tableId));
        if (request.getTableNumber() != null) {
            tableInfoRepository.findByStoreIdAndTableNumber(storeId, request.getTableNumber())
                    .filter(existing -> !existing.getTableId().equals(tableId))
                    .ifPresent(existing -> {
                        throw new BusinessException("桌號 " + request.getTableNumber() + " 在此門市已存在");
                    });
            table.setTableNumber(request.getTableNumber());
        }
        if (request.getTableSize() != null) table.setTableSize(request.getTableSize());
        if (request.getTableType() != null) table.setTableType(request.getTableType());
        if (request.getZone() != null) table.setZone(request.getZone());
        if (request.getStatus() != null) table.setStatus(request.getStatus());
        if (request.getIsCombinable() != null) table.setIsCombinable(request.getIsCombinable());
        return toTableResponse(tableInfoRepository.save(table));
    }

    @Override
    @Transactional
    public void deleteTable(Long storeId, Long tableId) {
        storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("門市", storeId));

        TableInfo table = tableInfoRepository.findByTableIdAndStoreId(tableId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("桌位", tableId));
        tableInfoRepository.delete(table);
    }

    // =================== Helper ===================

    private boolean isOpenNow(Long storeId) {
        LocalDate today = LocalDate.now();
        if (storeHolidayRepository.findByStoreIdAndHolidayDate(storeId, today).isPresent()) {
            return false;
        }
        int todayDow = today.getDayOfWeek().getValue(); // ISO: 週一=1, 週日=7
        LocalTime now = LocalTime.now();
        List<StoreHour> hours = storeHourRepository
                .findByStoreIdAndDayOfWeekAndIsClosedFalseOrderByOpenTimeAsc(storeId, todayDow);
        return hours.stream()
                .anyMatch(h -> !now.isBefore(h.getOpenTime()) && !now.isAfter(h.getCloseTime()));
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private StoreListResponse toListResponse(Store s, Double distanceKm) {
        return StoreListResponse.builder()
                .storeId(s.getStoreId())
                .storeCode(s.getStoreCode())
                .storeName(s.getStoreName())
                .city(s.getCity())
                .district(s.getDistrict())
                .address(s.getAddress())
                .phone(s.getPhone())
                .latitude(s.getLatitude())
                .longitude(s.getLongitude())
                .mainImageUrl(s.getMainImageUrl())
                .status(s.getStatus())
                .mrtInfo(s.getMrtInfo())
                .isOpenNow(isOpenNow(s.getStoreId()))
                .distanceKm(distanceKm)
                .build();
    }

    private StoreHourResponse toHourResponse(StoreHour h) {
        String[] dayNames = {"", "週一", "週二", "週三", "週四", "週五", "週六", "週日"};
        return StoreHourResponse.builder()
                .dayOfWeek(h.getDayOfWeek())
                .dayName(dayNames[h.getDayOfWeek()])
                .openTime(h.getOpenTime())
                .closeTime(h.getCloseTime())
                .mealPeriod(h.getMealPeriod())
                .isClosed(h.getIsClosed())
                .build();
    }

    private TableInfoResponse toTableResponse(TableInfo t) {
        return TableInfoResponse.builder()
                .tableId(t.getTableId())
                .tableNumber(t.getTableNumber())
                .tableSize(t.getTableSize())
                .tableType(t.getTableType())
                .zone(t.getZone())
                .status(t.getStatus())
                .isCombinable(t.getIsCombinable())
                .build();
    }

    private StoreHolidayResponse toHolidayResponse(StoreHoliday h) {
        return StoreHolidayResponse.builder()
                .holidayId(h.getHolidayId())
                .holidayDate(h.getHolidayDate())
                .reason(h.getReason())
                .build();
    }
}
