package com.restaurant.store.service.impl;

import com.restaurant.store.dto.request.NearbySearchRequest;
import com.restaurant.store.dto.response.StoreListResponse;
import com.restaurant.store.entity.Store;
import com.restaurant.store.entity.StoreHoliday;
import com.restaurant.store.entity.StoreHour;
import com.restaurant.store.repository.StoreFeatureRepository;
import com.restaurant.store.repository.StoreHolidayRepository;
import com.restaurant.store.repository.StoreHourRepository;
import com.restaurant.store.repository.StoreImageRepository;
import com.restaurant.store.repository.StoreRepository;
import com.restaurant.store.repository.TableInfoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreServiceImplTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StoreHourRepository storeHourRepository;

    @Mock
    private StoreHolidayRepository storeHolidayRepository;

    @Mock
    private StoreImageRepository storeImageRepository;

    @Mock
    private TableInfoRepository tableInfoRepository;

    @Mock
    private StoreFeatureRepository storeFeatureRepository;

    @InjectMocks
    private StoreServiceImpl storeService;

    @Test
    void findNearbyStoresSortsByDistanceAndAppliesLimit() {
        Store taipei = store(1L, "TPE001", "敘日信義 A11 店", "台北市", "信義區",
                25.0360390, 121.5674080);
        Store kaohsiung = store(2L, "KHH001", "敘日高雄夢時代店", "高雄市", "前鎮區",
                22.5955130, 120.3079830);

        when(storeRepository.findByIsDeletedFalseAndStatusOrderByCityAscDistrictAscStoreNameAsc("OPEN"))
                .thenReturn(List.of(kaohsiung, taipei));
        when(storeFeatureRepository.findByStoreIdInOrderByStoreIdAscSortOrderAscFeatureIdAsc(List.of(2L, 1L)))
                .thenReturn(List.of());
        when(storeHolidayRepository.findByStoreIdAndHolidayDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(storeHourRepository.findByStoreIdAndDayOfWeekAndIsClosedFalseOrderByOpenTimeAsc(anyLong(), anyInt()))
                .thenReturn(openAllDayHours());

        NearbySearchRequest request = new NearbySearchRequest();
        request.setLatitude(25.0359000);
        request.setLongitude(121.5669000);
        request.setLimit(1);

        List<StoreListResponse> result = storeService.findNearbyStores(request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStoreId()).isEqualTo(1L);
        assertThat(result.get(0).getDistanceKm()).isLessThan(1.0);
    }

    @Test
    void getAllOpenStoresMarksHolidayStoreAsClosedEvenWhenHoursAreOpen() {
        Store holidayStore = store(1L, "TPE001", "敘日信義 A11 店", "台北市", "信義區",
                25.0360390, 121.5674080);
        Store normalStore = store(2L, "TPE002", "敘日南港 CITYLINK 店", "台北市", "南港區",
                25.0527340, 121.6065700);

        when(storeRepository.findByIsDeletedFalseAndStatusOrderByCityAscDistrictAscStoreNameAsc("OPEN"))
                .thenReturn(List.of(holidayStore, normalStore));
        when(storeFeatureRepository.findByStoreIdInOrderByStoreIdAscSortOrderAscFeatureIdAsc(List.of(1L, 2L)))
                .thenReturn(List.of());
        when(storeHolidayRepository.findByStoreIdAndHolidayDate(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(StoreHoliday.builder().storeId(1L).holidayDate(LocalDate.now()).build()));
        when(storeHolidayRepository.findByStoreIdAndHolidayDate(eq(2L), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(storeHourRepository.findByStoreIdAndDayOfWeekAndIsClosedFalseOrderByOpenTimeAsc(eq(2L), anyInt()))
                .thenReturn(openAllDayHours());

        List<StoreListResponse> result = storeService.getAllOpenStores();

        assertThat(result).extracting(StoreListResponse::getStoreId).containsExactly(1L, 2L);
        assertThat(result.get(0).isOpenNow()).isFalse();
        assertThat(result.get(1).isOpenNow()).isTrue();
    }

    private static Store store(Long id, String code, String name, String city, String district,
                               double latitude, double longitude) {
        return Store.builder()
                .storeId(id)
                .brandId(1L)
                .storeCode(code)
                .storeName(name)
                .city(city)
                .district(district)
                .address(city + district + "示範路 1 號")
                .phone("02-0000-0000")
                .latitude(BigDecimal.valueOf(latitude))
                .longitude(BigDecimal.valueOf(longitude))
                .mrtInfo("捷運站步行 5 分鐘")
                .mainImageUrl("/store-images/xuri-dining-room.jpg")
                .status("OPEN")
                .isDeleted(false)
                .build();
    }

    private static List<StoreHour> openAllDayHours() {
        return List.of(StoreHour.builder()
                .storeId(1L)
                .dayOfWeek(LocalDate.now().getDayOfWeek().getValue())
                .openTime(LocalTime.MIN)
                .closeTime(LocalTime.MAX)
                .mealPeriod("ALL_DAY")
                .isClosed(false)
                .build());
    }
}
