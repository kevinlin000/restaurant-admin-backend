package com.restaurant.store.controller;

import com.restaurant.common.ApiResponse;
import com.restaurant.store.dto.request.NearbySearchRequest;
import com.restaurant.store.dto.response.StoreDetailResponse;
import com.restaurant.store.dto.response.StoreListResponse;
import com.restaurant.store.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
@Tag(name = "找門市", description = "門市查詢 API")
public class StoreController {

    private final StoreService storeService;

    @GetMapping
    @Operation(summary = "取得所有營業中門市")
    public ApiResponse<List<StoreListResponse>> getAllStores() {
        return ApiResponse.success(storeService.getAllOpenStores());
    }

    @GetMapping("/search")
    @Operation(summary = "關鍵字搜尋門市")
    public ApiResponse<List<StoreListResponse>> search(@RequestParam String keyword) {
        return ApiResponse.success(storeService.searchStores(keyword));
    }

    @GetMapping("/cities")
    @Operation(summary = "縣市下拉選單")
    public ApiResponse<List<String>> getCities() {
        return ApiResponse.success(storeService.getCities());
    }

    @GetMapping("/cities/{city}/districts")
    @Operation(summary = "區域下拉選單")
    public ApiResponse<List<String>> getDistricts(@PathVariable String city) {
        return ApiResponse.success(storeService.getDistrictsByCity(city));
    }

    @GetMapping("/city/{city}")
    @Operation(summary = "按縣市篩選")
    public ApiResponse<List<StoreListResponse>> getByCity(@PathVariable String city) {
        return ApiResponse.success(storeService.getStoresByCity(city));
    }

    @GetMapping("/city/{city}/district/{district}")
    @Operation(summary = "按縣市+區域篩選")
    public ApiResponse<List<StoreListResponse>> getByCityAndDistrict(
            @PathVariable String city, @PathVariable String district) {
        return ApiResponse.success(storeService.getStoresByCityAndDistrict(city, district));
    }

    @PostMapping("/nearby")
    @Operation(summary = "GPS 找最近門市（Haversine）")
    public ApiResponse<List<StoreListResponse>> nearby(@Valid @RequestBody NearbySearchRequest request) {
        return ApiResponse.success(storeService.findNearbyStores(request));
    }

    @GetMapping("/{storeId}")
    @Operation(summary = "門市詳細頁")
    public ApiResponse<StoreDetailResponse> getDetail(@PathVariable Long storeId) {
        return ApiResponse.success(storeService.getStoreDetail(storeId));
    }
}
