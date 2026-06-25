package com.restaurant.faq.controller;

import com.restaurant.common.ApiResponse;
import com.restaurant.faq.dto.FaqItemRequest;
import com.restaurant.faq.dto.FaqItemResponse;
import com.restaurant.faq.service.FaqItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/faqs")
@Tag(name = "後台常見問題管理", description = "FAQ 知識庫 CRUD")
public class FaqAdminController {

    private final FaqItemService faqItemService;

    @GetMapping
    @Operation(summary = "取得後台 FAQ 清單")
    public ApiResponse<List<FaqItemResponse>> getAdminFaqs(Authentication authentication) {
        return ApiResponse.success(faqItemService.getAdminFaqs(authentication));
    }

    @GetMapping("/{faqId}")
    @Operation(summary = "取得後台 FAQ 詳情")
    public ApiResponse<FaqItemResponse> getAdminFaqDetail(
            @PathVariable Long faqId,
            Authentication authentication) {
        return ApiResponse.success(faqItemService.getAdminFaqDetail(faqId, authentication));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "新增 FAQ")
    public ApiResponse<FaqItemResponse> createFaq(
            @Valid @RequestBody FaqItemRequest request,
            Authentication authentication) {
        return ApiResponse.success("常見問題已新增", faqItemService.createFaq(request, authentication));
    }

    @PutMapping("/{faqId}")
    @Operation(summary = "更新 FAQ")
    public ApiResponse<FaqItemResponse> updateFaq(
            @PathVariable Long faqId,
            @Valid @RequestBody FaqItemRequest request,
            Authentication authentication) {
        return ApiResponse.success("常見問題已更新", faqItemService.updateFaq(faqId, request, authentication));
    }

    @DeleteMapping("/{faqId}")
    @Operation(summary = "刪除 FAQ")
    public ApiResponse<Void> deleteFaq(@PathVariable Long faqId, Authentication authentication) {
        faqItemService.deleteFaq(faqId, authentication);
        return ApiResponse.success("常見問題已刪除");
    }
}
