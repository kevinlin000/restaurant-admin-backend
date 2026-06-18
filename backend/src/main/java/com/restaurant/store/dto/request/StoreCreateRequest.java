package com.restaurant.store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StoreCreateRequest {

    @NotNull(message = "品牌 ID 不可為空")
    private Long brandId;

    @NotBlank(message = "分店代號不可為空")
    @Size(max = 20)
    private String storeCode;

    @NotBlank(message = "門市名稱不可為空")
    @Size(max = 100)
    private String storeName;

    @NotBlank(message = "縣市不可為空")
    @Size(max = 20)
    private String city;

    @NotBlank(message = "區域不可為空")
    @Size(max = 20)
    private String district;

    @NotBlank(message = "地址不可為空")
    @Size(max = 200)
    private String address;

    @Size(max = 20)
    private String phone;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @Size(max = 100)
    private String mrtInfo;

    @Size(max = 200)
    private String parkingInfo;

    private String description;

    @Size(max = 500)
    private String mainImageUrl;
}
