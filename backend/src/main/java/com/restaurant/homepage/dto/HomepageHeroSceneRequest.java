package com.restaurant.homepage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HomepageHeroSceneRequest {

    @NotBlank(message = "情境名稱不可空白")
    @Size(max = 24, message = "情境名稱不可超過 24 字")
    private String label;

    @NotBlank(message = "圖片代碼不可空白")
    @Size(max = 60, message = "圖片代碼不可超過 60 字")
    private String imageKey;

    @Size(max = 500, message = "圖片網址不可超過 500 字")
    private String imageUrl;

    @NotBlank(message = "英文小標不可空白")
    @Size(max = 80, message = "英文小標不可超過 80 字")
    private String eyebrow;

    @NotBlank(message = "主標不可空白")
    @Size(max = 80, message = "主標不可超過 80 字")
    private String title;

    @Size(min = 1, max = 3, message = "每個情境需有 1 到 3 行文案")
    private List<@NotBlank(message = "情境文案不可空白") @Size(max = 80, message = "單行情境文案不可超過 80 字") String> lines = new ArrayList<>();
}
