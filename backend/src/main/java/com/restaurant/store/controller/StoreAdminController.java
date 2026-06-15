package com.restaurant.store.controller;

import com.restaurant.common.ApiResponse;
import com.restaurant.store.dto.request.*;
import com.restaurant.store.dto.response.StoreDetailResponse;
import com.restaurant.store.dto.response.StoreHolidayResponse;
import com.restaurant.store.dto.response.StoreHourResponse;
import com.restaurant.store.dto.response.StoreImageResponse;
import com.restaurant.store.dto.response.StoreListResponse;
import com.restaurant.store.dto.response.TableInfoResponse;
import com.restaurant.store.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "後台門市管理", description = "門市與桌位 CRUD")
public class StoreAdminController {

    private final StoreService storeService;

    // =================== 門市 ===================

    @GetMapping("/api/admin/stores")
    @Operation(summary = "取得後台門市清單")
    public ApiResponse<List<StoreListResponse>> getStores() {
        return ApiResponse.success(storeService.getAllStoresForAdmin());
    }

    @GetMapping("/api/admin/stores/{storeId}")
    @Operation(summary = "取得後台門市詳細資料")
    public ApiResponse<StoreDetailResponse> getStore(@PathVariable Long storeId) {
        return ApiResponse.success(storeService.getStoreDetailForAdmin(storeId));
    }

    @PostMapping("/api/admin/stores")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "新增門市")
    public ApiResponse<StoreDetailResponse> createStore(@Valid @RequestBody StoreCreateRequest request) {
        return ApiResponse.success("門市已新增", storeService.createStore(request));
    }

    @PutMapping("/api/admin/stores/{storeId}")
    @Operation(summary = "修改門市")
    public ApiResponse<StoreDetailResponse> updateStore(
            @PathVariable Long storeId, @Valid @RequestBody StoreUpdateRequest request) {
        return ApiResponse.success("門市已更新", storeService.updateStore(storeId, request));
    }

    @DeleteMapping("/api/admin/stores/{storeId}")
    @Operation(summary = "刪除門市（軟刪除）")
    public ApiResponse<Void> deleteStore(@PathVariable Long storeId) {
        storeService.deleteStore(storeId);
        return ApiResponse.success("門市已刪除");
    }

    // =================== 營業時間 ===================

    @GetMapping("/api/admin/stores/{storeId}/hours")
    @Operation(summary = "取得門市營業時間")
    public ApiResponse<List<StoreHourResponse>> getStoreHours(@PathVariable Long storeId) {
        return ApiResponse.success(storeService.getStoreHours(storeId));
    }

    @PostMapping("/api/admin/stores/{storeId}/hours")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "新增門市營業時間")
    public ApiResponse<StoreHourResponse> createStoreHour(
            @PathVariable Long storeId, @Valid @RequestBody StoreHourCreateRequest request) {
        return ApiResponse.success("營業時間已新增", storeService.createStoreHour(storeId, request));
    }

    @PutMapping("/api/admin/stores/{storeId}/hours/{hourId}")
    @Operation(summary = "修改門市營業時間")
    public ApiResponse<StoreHourResponse> updateStoreHour(
            @PathVariable Long storeId,
            @PathVariable Long hourId,
            @Valid @RequestBody StoreHourUpdateRequest request) {
        return ApiResponse.success("營業時間已更新", storeService.updateStoreHour(storeId, hourId, request));
    }

    @DeleteMapping("/api/admin/stores/{storeId}/hours/{hourId}")
    @Operation(summary = "刪除門市營業時間")
    public ApiResponse<Void> deleteStoreHour(@PathVariable Long storeId, @PathVariable Long hourId) {
        storeService.deleteStoreHour(storeId, hourId);
        return ApiResponse.success("營業時間已刪除");
    }

    // =================== 特殊公休日 ===================

    @GetMapping("/api/admin/stores/{storeId}/holidays")
    @Operation(summary = "取得門市特殊公休日")
    public ApiResponse<List<StoreHolidayResponse>> getStoreHolidays(@PathVariable Long storeId) {
        return ApiResponse.success(storeService.getStoreHolidays(storeId));
    }

    @PostMapping("/api/admin/stores/{storeId}/holidays")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "新增門市特殊公休日")
    public ApiResponse<StoreHolidayResponse> createStoreHoliday(
            @PathVariable Long storeId, @Valid @RequestBody StoreHolidayCreateRequest request) {
        return ApiResponse.success("公休日已新增", storeService.createStoreHoliday(storeId, request));
    }

    @PutMapping("/api/admin/stores/{storeId}/holidays/{holidayId}")
    @Operation(summary = "修改門市特殊公休日")
    public ApiResponse<StoreHolidayResponse> updateStoreHoliday(
            @PathVariable Long storeId,
            @PathVariable Long holidayId,
            @Valid @RequestBody StoreHolidayUpdateRequest request) {
        return ApiResponse.success("公休日已更新", storeService.updateStoreHoliday(storeId, holidayId, request));
    }

    @DeleteMapping("/api/admin/stores/{storeId}/holidays/{holidayId}")
    @Operation(summary = "刪除門市特殊公休日")
    public ApiResponse<Void> deleteStoreHoliday(@PathVariable Long storeId, @PathVariable Long holidayId) {
        storeService.deleteStoreHoliday(storeId, holidayId);
        return ApiResponse.success("公休日已刪除");
    }

    // =================== 門市圖片 ===================

    @GetMapping("/api/admin/stores/{storeId}/images")
    @Operation(summary = "取得門市圖片")
    public ApiResponse<List<StoreImageResponse>> getStoreImages(@PathVariable Long storeId) {
        return ApiResponse.success(storeService.getStoreImages(storeId));
    }

    @PostMapping("/api/admin/stores/{storeId}/images")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "新增門市圖片")
    public ApiResponse<StoreImageResponse> createStoreImage(
            @PathVariable Long storeId, @Valid @RequestBody StoreImageCreateRequest request) {
        return ApiResponse.success("門市圖片已新增", storeService.createStoreImage(storeId, request));
    }

    @PutMapping("/api/admin/stores/{storeId}/images/{imageId}")
    @Operation(summary = "修改門市圖片")
    public ApiResponse<StoreImageResponse> updateStoreImage(
            @PathVariable Long storeId,
            @PathVariable Long imageId,
            @Valid @RequestBody StoreImageUpdateRequest request) {
        return ApiResponse.success("門市圖片已更新", storeService.updateStoreImage(storeId, imageId, request));
    }

    @DeleteMapping("/api/admin/stores/{storeId}/images/{imageId}")
    @Operation(summary = "刪除門市圖片")
    public ApiResponse<Void> deleteStoreImage(@PathVariable Long storeId, @PathVariable Long imageId) {
        storeService.deleteStoreImage(storeId, imageId);
        return ApiResponse.success("門市圖片已刪除");
    }

    // =================== 桌位 ===================

    @GetMapping("/api/admin/stores/{storeId}/tables")
    @Operation(summary = "取得門市桌位清單")
    public ApiResponse<List<TableInfoResponse>> getTables(@PathVariable Long storeId) {
        return ApiResponse.success(storeService.getTablesByStore(storeId));
    }

    @PostMapping("/api/admin/stores/{storeId}/tables")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "新增桌位")
    public ApiResponse<TableInfoResponse> createTable(
            @PathVariable Long storeId, @Valid @RequestBody TableCreateRequest request) {
        return ApiResponse.success("桌位已新增", storeService.createTable(storeId, request));
    }

    @PutMapping("/api/admin/stores/{storeId}/tables/{tableId}")
    @Operation(summary = "修改桌位")
    public ApiResponse<TableInfoResponse> updateTable(
            @PathVariable Long storeId,
            @PathVariable Long tableId,
            @Valid @RequestBody TableUpdateRequest request) {
        return ApiResponse.success("桌位已更新", storeService.updateTable(storeId, tableId, request));
    }

    @DeleteMapping("/api/admin/stores/{storeId}/tables/{tableId}")
    @Operation(summary = "刪除桌位")
    public ApiResponse<Void> deleteTable(@PathVariable Long storeId, @PathVariable Long tableId) {
        storeService.deleteTable(storeId, tableId);
        return ApiResponse.success("桌位已刪除");
    }
}
