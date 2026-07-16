package com.restaurant.homepage.controller;

import com.restaurant.common.ApiResponse;
import com.restaurant.homepage.dto.HomepageSettingResponse;
import com.restaurant.homepage.service.HomepageSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/homepage")
@Tag(name = "前台首頁設定", description = "首頁公開設定")
public class HomepageController {

    private final HomepageSettingService homepageSettingService;

    @GetMapping
    @Operation(summary = "取得首頁公開設定")
    public ApiResponse<HomepageSettingResponse> getHomepageSetting() {
        return ApiResponse.success(homepageSettingService.getPublicSetting());
    }
}
