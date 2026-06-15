package com.restaurant.store.controller;

import com.restaurant.common.ApiResponse;
import com.restaurant.store.dto.request.*;
import com.restaurant.store.dto.response.StoreDetailResponse;
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
