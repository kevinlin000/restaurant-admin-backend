package com.restaurant.homepage.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.common.BusinessException;
import com.restaurant.homepage.dto.HomepageHeroSceneRequest;
import com.restaurant.homepage.dto.HomepageSettingRequest;
import com.restaurant.homepage.dto.HomepageSettingResponse;
import com.restaurant.homepage.entity.HomepageSetting;
import com.restaurant.homepage.repository.HomepageSettingRepository;
import com.restaurant.news.entity.NewsArticle;
import com.restaurant.news.repository.NewsArticleRepository;
import com.restaurant.store.entity.Store;
import com.restaurant.store.repository.StoreRepository;
import com.restaurant.store.service.StoreAdminAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomepageSettingServiceImplTest {

    @Mock
    private HomepageSettingRepository homepageSettingRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private NewsArticleRepository newsArticleRepository;

    @Mock
    private StoreAdminAccessService storeAdminAccessService;

    @Mock
    private Authentication authentication;

    private HomepageSettingServiceImpl homepageSettingService;

    @BeforeEach
    void setUp() {
        homepageSettingService = new HomepageSettingServiceImpl(
                homepageSettingRepository,
                storeRepository,
                newsArticleRepository,
                storeAdminAccessService,
                new ObjectMapper()
        );
    }

    @Test
    void publicSettingFallsBackToBrandDefaultsWhenNoSettingExists() {
        when(homepageSettingRepository.findById(1L)).thenReturn(Optional.empty());

        HomepageSettingResponse response = homepageSettingService.getPublicSetting();

        assertThat(response.getHeroScenes()).hasSize(3);
        assertThat(response.getHeroScenes().get(0).getImageKey()).isEqualTo("home");
        assertThat(response.getStoryTitle()).contains("一餐飯");
        assertThat(response.getFeaturedStoreIds()).isEmpty();
    }

    @Test
    void adminSettingRequiresAdminAccess() {
        when(homepageSettingRepository.findById(1L)).thenReturn(Optional.empty());

        homepageSettingService.getAdminSetting(authentication);

        verify(storeAdminAccessService).requireAdmin(authentication);
    }

    @Test
    void updateStoresTrimmedStructuredHomepageSetting() {
        HomepageSettingRequest request = request();
        request.setFeaturedStoreIds(List.of(2L, 2L));
        request.setFeaturedNewsIds(List.of(8L));
        when(homepageSettingRepository.findById(1L)).thenReturn(Optional.empty());
        when(storeRepository.findByStoreIdAndIsDeletedFalse(2L)).thenReturn(Optional.of(Store.builder().storeId(2L).build()));
        when(newsArticleRepository.findByNewsIdAndIsDeletedFalse(8L)).thenReturn(Optional.of(NewsArticle.builder().newsId(8L).build()));
        when(homepageSettingRepository.saveAndFlush(any(HomepageSetting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HomepageSettingResponse response = homepageSettingService.updateSetting(request, authentication);

        verify(storeAdminAccessService).requireAdmin(authentication);
        assertThat(response.getHeroScenes()).hasSize(1);
        assertThat(response.getHeroScenes().get(0).getLabel()).isEqualTo("夜席");
        assertThat(response.getStoryTitle()).isEqualTo("把忙碌留在門外");
        assertThat(response.getFeaturedStoreIds()).containsExactly(2L);
        assertThat(response.getFeaturedNewsIds()).containsExactly(8L);
    }

    @Test
    void updateRejectsUnknownFeaturedStore() {
        HomepageSettingRequest request = request();
        request.setFeaturedStoreIds(List.of(99L));
        when(storeRepository.findByStoreIdAndIsDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> homepageSettingService.updateSetting(request, authentication))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("找不到精選門市");
    }

    @Test
    void updateRejectsMoreThanThreeHeroScenes() {
        HomepageSettingRequest request = request();
        request.setHeroScenes(List.of(scene("一"), scene("二"), scene("三"), scene("四")));
        when(homepageSettingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> homepageSettingService.updateSetting(request, authentication))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("首頁情境需設定 1 到 3 組");
    }

    @Test
    void updateRejectsHeroSceneWithoutLines() {
        HomepageSettingRequest request = request();
        HomepageHeroSceneRequest scene = scene("夜席");
        scene.setLines(List.of());
        request.setHeroScenes(List.of(scene));
        when(homepageSettingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> homepageSettingService.updateSetting(request, authentication))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("首頁情境文案需設定 1 到 3 行");
    }

    private static HomepageSettingRequest request() {
        HomepageSettingRequest request = new HomepageSettingRequest();
        request.setHeroScenes(List.of(scene(" 夜席 ")));
        request.setStoryKicker("Xuri Table");
        request.setStoryTitle(" 把忙碌留在門外 ");
        request.setStoryDescription("品牌故事");
        request.setSignatureKicker("Seasonal Selection");
        request.setSignatureTitle("今天想吃的");
        request.setStoreKicker("Locations");
        request.setStoreTitle("今晚坐哪一間敘日");
        request.setStoreDescription("依城市挑選門市");
        request.setNewsKicker("News");
        request.setNewsTitle("近期公告");
        request.setReservationKicker("Reservation");
        request.setReservationTitle("選一張剛好的桌");
        request.setReservationDescription("訂位說明");
        return request;
    }

    private static HomepageHeroSceneRequest scene(String label) {
        HomepageHeroSceneRequest scene = new HomepageHeroSceneRequest();
        scene.setLabel(label);
        scene.setImageKey("home");
        scene.setEyebrow("Xuri Washoku");
        scene.setTitle("敘日和食");
        scene.setLines(List.of("第一行", "第二行"));
        return scene;
    }
}
