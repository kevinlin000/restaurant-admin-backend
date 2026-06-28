package com.restaurant.faq.controller;

import com.restaurant.common.ApiResponse;
import com.restaurant.faq.dto.FaqItemResponse;
import com.restaurant.faq.dto.FaqSearchResponse;
import com.restaurant.faq.entity.FaqCategory;
import com.restaurant.faq.service.FaqItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/faqs")
@Tag(name = "前台常見問題", description = "常見問題與客服搜尋")
public class FaqController {

    private final FaqItemService faqItemService;

    @GetMapping
    @Operation(summary = "取得已發布常見問題")
    public ApiResponse<List<FaqItemResponse>> getPublishedFaqs(
            @RequestParam(required = false) FaqCategory category) {
        return ApiResponse.success(faqItemService.getPublishedFaqs(category));
    }

    @GetMapping("/search")
    @Operation(summary = "搜尋常見問題")
    public ApiResponse<FaqSearchResponse> searchFaqs(@RequestParam(required = false) String q) {
        return ApiResponse.success(faqItemService.searchPublishedFaqs(q));
    }
}
