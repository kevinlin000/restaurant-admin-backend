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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final StoreHourRepository storeHourRepository;
    private final StoreHolidayRepository storeHolidayRepository;
    private final StoreImageRepository storeImageRepository;
    private final TableInfoRepository tableInfoRepository;
    private final StoreFeatureRepository storeFeatureRepository;

    // =================== 前台 ===================

    @Override
    @Transactional(readOnly = true)
    public List<StoreListResponse> getAllOpenStores() {
        List<Store> stores = storeRepository.findByIsDeletedFalseAndStatusOrderByCityAscDistrictAscStoreNameAsc("OPEN");
        Map<Long, List<StoreFeatureResponse>> featureMap = loadFeatureMap(stores);
        return stores.stream()
                .map(s -> toListResponse(s, null, featureMap.getOrDefault(s.getStoreId(), List.of())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreListResponse> searchStores(String keyword) {
        List<Store> stores = storeRepository.searchByKeyword(keyword);
        Map<Long, List<StoreFeatureResponse>> featureMap = loadFeatureMap(stores);
        return stores.stream()
                .map(s -> toListResponse(s, null, featureMap.getOrDefault(s.getStoreId(), List.of())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreListResponse> getStoresByCity(String city) {
        List<Store> stores = storeRepository.findByIsDeletedFalseAndStatusAndCityOrderByDistrictAscStoreNameAsc("OPEN", city);
        Map<Long, List<StoreFeatureResponse>> featureMap = loadFeatureMap(stores);
        return stores.stream()
                .map(s -> toListResponse(s, null, featureMap.getOrDefault(s.getStoreId(), List.of())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreListResponse> getStoresByCityAndDistrict(String city, String district) {
        List<Store> stores = storeRepository.findByIsDeletedFalseAndStatusAndCityAndDistrictOrderByStoreNameAsc("OPEN", city, district);
        Map<Long, List<StoreFeatureResponse>> featureMap = loadFeatureMap(stores);
        return stores.stream()
                .map(s -> toListResponse(s, null, featureMap.getOrDefault(s.getStoreId(), List.of())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreListResponse> findNearbyStores(NearbySearchRequest request) {
        List<Store> stores = storeRepository.findByIsDeletedFalseAndStatusOrderByCityAscDistrictAscStoreNameAsc("OPEN");
        Map<Long, List<StoreFeatureResponse>> featureMap = loadFeatureMap(stores);
        return stores
                .stream()
                .filter(s -> s.getLatitude() != null && s.getLongitude() != null)
                .map(s -> {
                    double dist = haversineKm(
                            request.getLatitude(), request.getLongitude(),
                            s.getLatitude().doubleValue(), s.getLongitude().doubleValue());
                    return toListResponse(s, dist, featureMap.getOrDefault(s.getStoreId(), List.of()));
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
        List<StoreFeatureResponse> features = storeFeatureRepository.findByStoreIdOrderBySortOrderAscFeatureIdAsc(storeId)
                .stream()
                .map(this::toFeatureResponse)
                .collect(Collectors.toList());
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
                .featureTags(features)
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
        List<Store> stores = storeRepository.findByIsDeletedFalseOrderByCityAscDistrictAscStoreNameAsc();
        Map<Long, List<StoreFeatureResponse>> featureMap = loadFeatureMap(stores);
        return stores.stream()
                .map(s -> toListResponse(s, null, featureMap.getOrDefault(s.getStoreId(), List.of())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StoreDetailResponse getStoreDetailForAdmin(Long storeId) {
        return getStoreDetail(storeId);
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
    public List<StoreHourResponse> getStoreHours(Long storeId) {
        requireStore(storeId);
        return storeHourRepository.findByStoreIdOrderByDayOfWeekAscMealPeriodAscOpenTimeAsc(storeId)
                .stream().map(this::toHourResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StoreHourResponse createStoreHour(Long storeId, StoreHourCreateRequest request) {
        requireStore(storeId);
        validateHourRange(request.getOpenTime(), request.getCloseTime(), Boolean.TRUE.equals(request.getIsClosed()));
        ensureUniqueHour(storeId, request.getDayOfWeek(), request.getMealPeriod(), null);

        StoreHour hour = StoreHour.builder()
                .storeId(storeId)
                .dayOfWeek(request.getDayOfWeek())
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .mealPeriod(request.getMealPeriod())
                .isClosed(Boolean.TRUE.equals(request.getIsClosed()))
                .build();
        return toHourResponse(storeHourRepository.save(hour));
    }

    @Override
    @Transactional
    public StoreHourResponse updateStoreHour(Long storeId, Long hourId, StoreHourUpdateRequest request) {
        requireStore(storeId);
        StoreHour hour = storeHourRepository.findByHourIdAndStoreId(hourId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("營業時間", hourId));

        Integer dayOfWeek = request.getDayOfWeek() != null ? request.getDayOfWeek() : hour.getDayOfWeek();
        String mealPeriod = request.getMealPeriod() != null ? request.getMealPeriod() : hour.getMealPeriod();
        LocalTime openTime = request.getOpenTime() != null ? request.getOpenTime() : hour.getOpenTime();
        LocalTime closeTime = request.getCloseTime() != null ? request.getCloseTime() : hour.getCloseTime();
        Boolean isClosed = request.getIsClosed() != null ? request.getIsClosed() : hour.getIsClosed();

        validateHourRange(openTime, closeTime, Boolean.TRUE.equals(isClosed));
        ensureUniqueHour(storeId, dayOfWeek, mealPeriod, hourId);

        hour.setDayOfWeek(dayOfWeek);
        hour.setMealPeriod(mealPeriod);
        hour.setOpenTime(openTime);
        hour.setCloseTime(closeTime);
        hour.setIsClosed(isClosed);
        return toHourResponse(storeHourRepository.save(hour));
    }

    @Override
    @Transactional
    public void deleteStoreHour(Long storeId, Long hourId) {
        requireStore(storeId);
        StoreHour hour = storeHourRepository.findByHourIdAndStoreId(hourId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("營業時間", hourId));
        storeHourRepository.delete(hour);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreHolidayResponse> getStoreHolidays(Long storeId) {
        requireStore(storeId);
        return storeHolidayRepository.findByStoreIdOrderByHolidayDateAsc(storeId)
                .stream().map(this::toHolidayResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StoreHolidayResponse createStoreHoliday(Long storeId, StoreHolidayCreateRequest request) {
        requireStore(storeId);
        storeHolidayRepository.findByStoreIdAndHolidayDate(storeId, request.getHolidayDate())
                .ifPresent(existing -> {
                    throw new BusinessException("此日期已設定公休：" + request.getHolidayDate());
                });

        StoreHoliday holiday = StoreHoliday.builder()
                .storeId(storeId)
                .holidayDate(request.getHolidayDate())
                .reason(request.getReason())
                .build();
        return toHolidayResponse(storeHolidayRepository.save(holiday));
    }

    @Override
    @Transactional
    public StoreHolidayResponse updateStoreHoliday(Long storeId, Long holidayId, StoreHolidayUpdateRequest request) {
        requireStore(storeId);
        StoreHoliday holiday = storeHolidayRepository.findByHolidayIdAndStoreId(holidayId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("公休日", holidayId));

        storeHolidayRepository.findByStoreIdAndHolidayDate(storeId, request.getHolidayDate())
                .filter(existing -> !existing.getHolidayId().equals(holidayId))
                .ifPresent(existing -> {
                    throw new BusinessException("此日期已設定公休：" + request.getHolidayDate());
                });

        holiday.setHolidayDate(request.getHolidayDate());
        holiday.setReason(request.getReason());
        return toHolidayResponse(storeHolidayRepository.save(holiday));
    }

    @Override
    @Transactional
    public void deleteStoreHoliday(Long storeId, Long holidayId) {
        requireStore(storeId);
        StoreHoliday holiday = storeHolidayRepository.findByHolidayIdAndStoreId(holidayId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("公休日", holidayId));
        storeHolidayRepository.delete(holiday);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreImageResponse> getStoreImages(Long storeId) {
        requireStore(storeId);
        return storeImageRepository.findByStoreIdOrderBySortOrderAsc(storeId)
                .stream().map(this::toImageResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StoreImageResponse createStoreImage(Long storeId, StoreImageCreateRequest request) {
        requireStore(storeId);
        StoreImage image = StoreImage.builder()
                .storeId(storeId)
                .imageUrl(request.getImageUrl())
                .caption(request.getCaption())
                .sortOrder(request.getSortOrder())
                .build();
        return toImageResponse(storeImageRepository.save(image));
    }

    @Override
    @Transactional
    public StoreImageResponse updateStoreImage(Long storeId, Long imageId, StoreImageUpdateRequest request) {
        requireStore(storeId);
        StoreImage image = storeImageRepository.findByImageIdAndStoreId(imageId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("門市圖片", imageId));
        if (request.getImageUrl() != null) image.setImageUrl(request.getImageUrl());
        if (request.getCaption() != null) image.setCaption(request.getCaption());
        if (request.getSortOrder() != null) image.setSortOrder(request.getSortOrder());
        return toImageResponse(storeImageRepository.save(image));
    }

    @Override
    @Transactional
    public void deleteStoreImage(Long storeId, Long imageId) {
        requireStore(storeId);
        StoreImage image = storeImageRepository.findByImageIdAndStoreId(imageId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("門市圖片", imageId));
        storeImageRepository.delete(image);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreFeatureResponse> getStoreFeatures(Long storeId) {
        requireStore(storeId);
        return storeFeatureRepository.findByStoreIdOrderBySortOrderAscFeatureIdAsc(storeId)
                .stream().map(this::toFeatureResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StoreFeatureResponse createStoreFeature(Long storeId, StoreFeatureCreateRequest request) {
        requireStore(storeId);
        String featureKey = normalizeFeatureKey(request.getFeatureKey());
        storeFeatureRepository.findByStoreIdAndFeatureKey(storeId, featureKey)
                .ifPresent(existing -> {
                    throw new BusinessException("此門市已存在相同特色標籤：" + featureKey);
                });

        StoreFeature feature = StoreFeature.builder()
                .storeId(storeId)
                .featureKey(featureKey)
                .featureLabel(request.getFeatureLabel().trim())
                .sortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder())
                .build();
        return toFeatureResponse(storeFeatureRepository.save(feature));
    }

    @Override
    @Transactional
    public StoreFeatureResponse updateStoreFeature(Long storeId, Long featureId, StoreFeatureUpdateRequest request) {
        requireStore(storeId);
        StoreFeature feature = storeFeatureRepository.findByFeatureIdAndStoreId(featureId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("門市特色標籤", featureId));
        String featureKey = normalizeFeatureKey(request.getFeatureKey());
        storeFeatureRepository.findByStoreIdAndFeatureKey(storeId, featureKey)
                .filter(existing -> !existing.getFeatureId().equals(featureId))
                .ifPresent(existing -> {
                    throw new BusinessException("此門市已存在相同特色標籤：" + featureKey);
                });

        feature.setFeatureKey(featureKey);
        feature.setFeatureLabel(request.getFeatureLabel().trim());
        feature.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        return toFeatureResponse(storeFeatureRepository.save(feature));
    }

    @Override
    @Transactional
    public void deleteStoreFeature(Long storeId, Long featureId) {
        requireStore(storeId);
        StoreFeature feature = storeFeatureRepository.findByFeatureIdAndStoreId(featureId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("門市特色標籤", featureId));
        storeFeatureRepository.delete(feature);
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

    private Store requireStore(Long storeId) {
        return storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("門市", storeId));
    }

    private void validateHourRange(LocalTime openTime, LocalTime closeTime, boolean isClosed) {
        if (openTime == null || closeTime == null) {
            throw new BusinessException("開店與關店時間不可為空");
        }
        if (isClosed) {
            return;
        }
        if (!closeTime.isAfter(openTime)) {
            throw new BusinessException("關店時間需晚於開店時間");
        }
    }

    private void ensureUniqueHour(Long storeId, Integer dayOfWeek, String mealPeriod, Long currentHourId) {
        storeHourRepository.findByStoreIdAndDayOfWeekAndMealPeriod(storeId, dayOfWeek, mealPeriod)
                .filter(existing -> currentHourId == null || !existing.getHourId().equals(currentHourId))
                .ifPresent(existing -> {
                    throw new BusinessException("此星期與時段已設定營業時間");
                });
    }

    private String normalizeFeatureKey(String featureKey) {
        return featureKey == null ? "" : featureKey.trim().toUpperCase();
    }

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

    private Map<Long, List<StoreFeatureResponse>> loadFeatureMap(List<Store> stores) {
        List<Long> storeIds = stores.stream().map(Store::getStoreId).toList();
        if (storeIds.isEmpty()) {
            return Map.of();
        }
        return storeFeatureRepository.findByStoreIdInOrderByStoreIdAscSortOrderAscFeatureIdAsc(storeIds)
                .stream()
                .collect(Collectors.groupingBy(
                        StoreFeature::getStoreId,
                        Collectors.mapping(this::toFeatureResponse, Collectors.toList())));
    }

    private StoreListResponse toListResponse(Store s, Double distanceKm, List<StoreFeatureResponse> features) {
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
                .featureTags(features)
                .build();
    }

    private StoreHourResponse toHourResponse(StoreHour h) {
        String[] dayNames = {"", "週一", "週二", "週三", "週四", "週五", "週六", "週日"};
        return StoreHourResponse.builder()
                .hourId(h.getHourId())
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

    private StoreImageResponse toImageResponse(StoreImage image) {
        return StoreImageResponse.builder()
                .imageId(image.getImageId())
                .imageUrl(image.getImageUrl())
                .caption(image.getCaption())
                .sortOrder(image.getSortOrder())
                .build();
    }

    private StoreFeatureResponse toFeatureResponse(StoreFeature feature) {
        return StoreFeatureResponse.builder()
                .featureId(feature.getFeatureId())
                .featureKey(feature.getFeatureKey())
                .featureLabel(feature.getFeatureLabel())
                .sortOrder(feature.getSortOrder())
                .build();
    }
}
