package com.restaurant.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreDetailResponse {
    private Long storeId;
    private String storeCode;
    private String storeName;
    private String city;
    private String district;
    private String address;
    private String phone;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String mainImageUrl;
    private String status;
    private String mrtInfo;
    private String parkingInfo;
    private String description;
    private boolean isOpenNow;
    private List<StoreHourResponse> storeHours;
    private List<String> imageUrls;
    private List<TableInfoResponse> tables;
    private List<StoreHolidayResponse> upcomingHolidays;
}
