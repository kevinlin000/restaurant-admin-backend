package com.restaurant.store.service;

import com.restaurant.store.dto.request.*;
import com.restaurant.store.dto.response.*;

import java.util.List;

public interface StoreService {

    // 前台
    List<StoreListResponse> getAllOpenStores();
    List<StoreListResponse> searchStores(String keyword);
    List<StoreListResponse> getStoresByCity(String city);
    List<StoreListResponse> getStoresByCityAndDistrict(String city, String district);
    List<StoreListResponse> findNearbyStores(NearbySearchRequest request);
    StoreDetailResponse getStoreDetail(Long storeId);
    List<String> getCities();
    List<String> getDistrictsByCity(String city);

    // 後台
    List<StoreListResponse> getAllStoresForAdmin();
    StoreDetailResponse getStoreDetailForAdmin(Long storeId);
    StoreDetailResponse createStore(StoreCreateRequest request);
    StoreDetailResponse updateStore(Long storeId, StoreUpdateRequest request);
    void deleteStore(Long storeId);
    List<StoreHourResponse> getStoreHours(Long storeId);
    StoreHourResponse createStoreHour(Long storeId, StoreHourCreateRequest request);
    StoreHourResponse updateStoreHour(Long storeId, Long hourId, StoreHourUpdateRequest request);
    void deleteStoreHour(Long storeId, Long hourId);
    List<StoreHolidayResponse> getStoreHolidays(Long storeId);
    StoreHolidayResponse createStoreHoliday(Long storeId, StoreHolidayCreateRequest request);
    StoreHolidayResponse updateStoreHoliday(Long storeId, Long holidayId, StoreHolidayUpdateRequest request);
    void deleteStoreHoliday(Long storeId, Long holidayId);
    List<StoreImageResponse> getStoreImages(Long storeId);
    StoreImageResponse createStoreImage(Long storeId, StoreImageCreateRequest request);
    StoreImageResponse updateStoreImage(Long storeId, Long imageId, StoreImageUpdateRequest request);
    void deleteStoreImage(Long storeId, Long imageId);
    List<TableInfoResponse> getTablesByStore(Long storeId);
    TableInfoResponse createTable(Long storeId, TableCreateRequest request);
    TableInfoResponse updateTable(Long storeId, Long tableId, TableUpdateRequest request);
    void deleteTable(Long storeId, Long tableId);
}
