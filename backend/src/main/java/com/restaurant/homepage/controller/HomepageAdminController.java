package com.restaurant.homepage.controller;

import com.restaurant.common.ApiResponse;
import com.restaurant.homepage.dto.HomepageSettingRequest;
import com.restaurant.homepage.dto.HomepageSettingResponse;
import com.restaurant.homepage.service.HomepageSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/homepage")
@Tag(name = "後台首頁管理", description = "首頁門面內容管理")
public class HomepageAdminController {

    private final HomepageSettingService homepageSettingService;

    @GetMapping
    @Operation(summary = "取得首頁後台設定")
    public ApiResponse<HomepageSettingResponse> getAdminHomepageSetting(Authentication authentication) {
        return ApiResponse.success(homepageSettingService.getAdminSetting(authentication));
    }

    @PutMapping
    @Operation(summary = "更新首頁設定")
    public ApiResponse<HomepageSettingResponse> updateHomepageSetting(
            @Valid @RequestBody HomepageSettingRequest request,
            Authentication authentication) {
        return ApiResponse.success("首頁設定已更新", homepageSettingService.updateSetting(request, authentication));
    }
}
