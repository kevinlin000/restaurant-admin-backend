package com.restaurant.order.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.order.dto.TopMenuItemResponse;
import com.restaurant.order.service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuRecommendController {

    private final DashboardService dashboardService;

    @GetMapping("/recommend")
    public List<TopMenuItemResponse> getRecommendMenuItems() {
        return dashboardService.getTopMenuItems();
    }
}
