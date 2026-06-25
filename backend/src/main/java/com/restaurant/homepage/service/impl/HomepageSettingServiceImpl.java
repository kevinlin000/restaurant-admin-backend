package com.restaurant.homepage.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.common.BusinessException;
import com.restaurant.homepage.dto.HomepageHeroSceneRequest;
import com.restaurant.homepage.dto.HomepageHeroSceneResponse;
import com.restaurant.homepage.dto.HomepageSettingRequest;
import com.restaurant.homepage.dto.HomepageSettingResponse;
import com.restaurant.homepage.entity.HomepageSetting;
import com.restaurant.homepage.repository.HomepageSettingRepository;
import com.restaurant.homepage.service.HomepageSettingService;
import com.restaurant.news.repository.NewsArticleRepository;
import com.restaurant.store.repository.StoreRepository;
import com.restaurant.store.service.StoreAdminAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class HomepageSettingServiceImpl implements HomepageSettingService {

    private static final Long SINGLETON_ID = 1L;

    private final HomepageSettingRepository homepageSettingRepository;
    private final StoreRepository storeRepository;
    private final NewsArticleRepository newsArticleRepository;
    private final StoreAdminAccessService storeAdminAccessService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public HomepageSettingResponse getPublicSetting() {
        return homepageSettingRepository.findById(SINGLETON_ID)
                .map(this::toResponse)
                .orElseGet(this::defaultResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public HomepageSettingResponse getAdminSetting(Authentication authentication) {
        storeAdminAccessService.requireAdmin(authentication);
        return getPublicSetting();
    }

    @Override
    @Transactional
    public HomepageSettingResponse updateSetting(HomepageSettingRequest request, Authentication authentication) {
        storeAdminAccessService.requireAdmin(authentication);
        validateReferences(request);

        HomepageSetting setting = homepageSettingRepository.findById(SINGLETON_ID)
                .orElseGet(() -> HomepageSetting.builder().settingId(SINGLETON_ID).build());

        setting.setHeroScenesJson(writeHeroScenes(request.getHeroScenes()));
        setting.setStoryKicker(cleanRequired(request.getStoryKicker()));
        setting.setStoryTitle(cleanRequired(request.getStoryTitle()));
        setting.setStoryDescription(cleanRequired(request.getStoryDescription()));
        setting.setSignatureKicker(cleanRequired(request.getSignatureKicker()));
        setting.setSignatureTitle(cleanRequired(request.getSignatureTitle()));
        setting.setStoreKicker(cleanRequired(request.getStoreKicker()));
        setting.setStoreTitle(cleanRequired(request.getStoreTitle()));
        setting.setStoreDescription(cleanRequired(request.getStoreDescription()));
        setting.setNewsKicker(cleanRequired(request.getNewsKicker()));
        setting.setNewsTitle(cleanRequired(request.getNewsTitle()));
        setting.setReservationKicker(cleanRequired(request.getReservationKicker()));
        setting.setReservationTitle(cleanRequired(request.getReservationTitle()));
        setting.setReservationDescription(cleanRequired(request.getReservationDescription()));
        setting.setFeaturedStoreIds(joinIds(request.getFeaturedStoreIds()));
        setting.setFeaturedNewsIds(joinIds(request.getFeaturedNewsIds()));

        return toResponse(homepageSettingRepository.saveAndFlush(setting));
    }

    private void validateReferences(HomepageSettingRequest request) {
        for (Long storeId : cleanIds(request.getFeaturedStoreIds())) {
            storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
                    .orElseThrow(() -> new BusinessException("找不到精選門市：" + storeId));
        }

        for (Long newsId : cleanIds(request.getFeaturedNewsIds())) {
            newsArticleRepository.findByNewsIdAndIsDeletedFalse(newsId)
                    .orElseThrow(() -> new BusinessException("找不到精選消息：" + newsId));
        }
    }

    private HomepageSettingResponse toResponse(HomepageSetting setting) {
        return HomepageSettingResponse.builder()
                .heroScenes(readHeroScenes(setting.getHeroScenesJson()))
                .storyKicker(setting.getStoryKicker())
                .storyTitle(setting.getStoryTitle())
                .storyDescription(setting.getStoryDescription())
                .signatureKicker(setting.getSignatureKicker())
                .signatureTitle(setting.getSignatureTitle())
                .storeKicker(setting.getStoreKicker())
                .storeTitle(setting.getStoreTitle())
                .storeDescription(setting.getStoreDescription())
                .newsKicker(setting.getNewsKicker())
                .newsTitle(setting.getNewsTitle())
                .reservationKicker(setting.getReservationKicker())
                .reservationTitle(setting.getReservationTitle())
                .reservationDescription(setting.getReservationDescription())
                .featuredStoreIds(splitIds(setting.getFeaturedStoreIds()))
                .featuredNewsIds(splitIds(setting.getFeaturedNewsIds()))
                .updatedAt(setting.getUpdatedAt())
                .build();
    }

    private HomepageSettingResponse defaultResponse() {
        return HomepageSettingResponse.builder()
                .heroScenes(defaultHeroScenes())
                .storyKicker("Xuri Table")
                .storyTitle("把忙碌留在門外，讓一餐飯重新有時間感。")
                .storyDescription("敘日把和食做成日常可以靠近的樣子：保留刺身、握壽司、鍋物與甘味的細節，也把訂位、門市與菜單動線整理成清楚的節奏。從進門、點餐到結帳，每個環節都為聚餐的人服務。")
                .signatureKicker("Seasonal Selection")
                .signatureTitle("今天想吃的，不只是一份菜單。")
                .storeKicker("Locations")
                .storeTitle("今晚坐哪一間敘日。")
                .storeDescription("依城市、交通與用餐情境挑選門市，讓前往餐桌這件事也輕鬆。")
                .newsKicker("News")
                .newsTitle("近期公告與餐期更新。")
                .reservationKicker("Reservation")
                .reservationTitle("從今天的城市，選一張剛好的桌。")
                .reservationDescription("選擇門市、日期、人數與時段後，系統會依席位與營業狀態回覆可訂選項。團體聚餐或特殊需求，也可以在訂位備註中先告訴店長。")
                .featuredStoreIds(List.of())
                .featuredNewsIds(List.of())
                .build();
    }

    private List<HomepageHeroSceneResponse> defaultHeroScenes() {
        return List.of(
                HomepageHeroSceneResponse.builder()
                        .label("夜席")
                        .imageKey("home")
                        .eyebrow("Xuri Washoku")
                        .title("敘日和食")
                        .lines(List.of("「敘」是敘舊，是放下手機後的深度對談", "「日」是時光，是歲月淬鍊出的滋味"))
                        .build(),
                HomepageHeroSceneResponse.builder()
                        .label("旬味")
                        .imageKey("salmonSashimi")
                        .eyebrow("Seasonal Sashimi")
                        .title("今日旬味")
                        .lines(List.of("低溫配送的魚身甜度", "讓聚餐從第一口開始慢下來"))
                        .build(),
                HomepageHeroSceneResponse.builder()
                        .label("吧台")
                        .imageKey("reservation")
                        .eyebrow("Chef Counter")
                        .title("一席之間")
                        .lines(List.of("從訂位到上菜都有清楚節奏", "把用餐體驗交給剛好的時間"))
                        .build()
        );
    }

    private String writeHeroScenes(List<HomepageHeroSceneRequest> scenes) {
        List<HomepageHeroSceneResponse> cleanedScenes = scenes == null ? defaultHeroScenes() : scenes.stream()
                .filter(Objects::nonNull)
                .map(scene -> {
                    List<String> lines = scene.getLines() == null ? List.<String>of() : scene.getLines().stream()
                            .map(this::cleanRequired)
                            .toList();
                    if (lines.isEmpty() || lines.size() > 3) {
                        throw new BusinessException("首頁情境文案需設定 1 到 3 行");
                    }
                    return HomepageHeroSceneResponse.builder()
                            .label(cleanRequired(scene.getLabel()))
                            .imageKey(cleanRequired(scene.getImageKey()))
                            .imageUrl(cleanNullable(scene.getImageUrl()))
                            .eyebrow(cleanRequired(scene.getEyebrow()))
                            .title(cleanRequired(scene.getTitle()))
                            .lines(lines)
                            .build();
                })
                .toList();

        if (cleanedScenes.isEmpty() || cleanedScenes.size() > 3) {
            throw new BusinessException("首頁情境需設定 1 到 3 組");
        }

        try {
            return objectMapper.writeValueAsString(cleanedScenes);
        } catch (JsonProcessingException e) {
            throw new BusinessException("首頁情境設定格式錯誤");
        }
    }

    private List<HomepageHeroSceneResponse> readHeroScenes(String json) {
        if (json == null || json.isBlank()) {
            return defaultHeroScenes();
        }

        try {
            List<HomepageHeroSceneResponse> scenes = objectMapper.readValue(
                    json,
                    new TypeReference<List<HomepageHeroSceneResponse>>() {
                    }
            );
            return scenes == null || scenes.isEmpty() ? defaultHeroScenes() : scenes;
        } catch (JsonProcessingException e) {
            return defaultHeroScenes();
        }
    }

    private String joinIds(List<Long> ids) {
        List<Long> cleanedIds = cleanIds(ids);
        if (cleanedIds.isEmpty()) {
            return null;
        }
        return cleanedIds.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse(null);
    }

    private List<Long> splitIds(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        List<Long> ids = new ArrayList<>();
        for (String token : value.split(",")) {
            try {
                ids.add(Long.parseLong(token.trim()));
            } catch (NumberFormatException ignored) {
                return List.of();
            }
        }
        return ids;
    }

    private List<Long> cleanIds(List<Long> ids) {
        if (ids == null) {
            return List.of();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private String cleanRequired(String value) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.isBlank()) {
            throw new BusinessException("首頁設定欄位不可空白");
        }
        return cleaned;
    }

    private String cleanNullable(String value) {
        String cleaned = value == null ? null : value.trim();
        return cleaned == null || cleaned.isBlank() ? null : cleaned;
    }
}
