package com.restaurant.homepage.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HomepageSettingRequest {

    @Size(min = 1, max = 3, message = "首頁情境需設定 1 到 3 組")
    private List<@Valid HomepageHeroSceneRequest> heroScenes = new ArrayList<>();

    @NotBlank(message = "品牌故事小標不可空白")
    @Size(max = 80)
    private String storyKicker;

    @NotBlank(message = "品牌故事標題不可空白")
    @Size(max = 160)
    private String storyTitle;

    @NotBlank(message = "品牌故事內文不可空白")
    @Size(max = 700)
    private String storyDescription;

    @NotBlank(message = "精選菜單小標不可空白")
    @Size(max = 80)
    private String signatureKicker;

    @NotBlank(message = "精選菜單標題不可空白")
    @Size(max = 160)
    private String signatureTitle;

    @NotBlank(message = "門市區小標不可空白")
    @Size(max = 80)
    private String storeKicker;

    @NotBlank(message = "門市區標題不可空白")
    @Size(max = 160)
    private String storeTitle;

    @NotBlank(message = "門市區內文不可空白")
    @Size(max = 360)
    private String storeDescription;

    @NotBlank(message = "消息區小標不可空白")
    @Size(max = 80)
    private String newsKicker;

    @NotBlank(message = "消息區標題不可空白")
    @Size(max = 160)
    private String newsTitle;

    @NotBlank(message = "訂位區小標不可空白")
    @Size(max = 80)
    private String reservationKicker;

    @NotBlank(message = "訂位區標題不可空白")
    @Size(max = 160)
    private String reservationTitle;

    @NotBlank(message = "訂位區內文不可空白")
    @Size(max = 700)
    private String reservationDescription;

    @Size(max = 6, message = "精選門市最多 6 間")
    private List<Long> featuredStoreIds = new ArrayList<>();

    @Size(max = 6, message = "精選消息最多 6 則")
    private List<Long> featuredNewsIds = new ArrayList<>();
}
