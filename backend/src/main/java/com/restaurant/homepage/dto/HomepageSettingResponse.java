package com.restaurant.homepage.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class HomepageSettingResponse {
    private List<HomepageHeroSceneResponse> heroScenes;
    private String storyKicker;
    private String storyTitle;
    private String storyDescription;
    private String signatureKicker;
    private String signatureTitle;
    private String storeKicker;
    private String storeTitle;
    private String storeDescription;
    private String newsKicker;
    private String newsTitle;
    private String reservationKicker;
    private String reservationTitle;
    private String reservationDescription;
    private List<Long> featuredStoreIds;
    private List<Long> featuredNewsIds;
    private LocalDateTime updatedAt;
}
