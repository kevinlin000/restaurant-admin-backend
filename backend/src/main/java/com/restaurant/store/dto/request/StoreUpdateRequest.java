package com.restaurant.store.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StoreUpdateRequest {

    @Size(max = 100)
    private String storeName;

    @Size(max = 20)
    private String city;

    @Size(max = 20)
    private String district;

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

    @Pattern(regexp = "PREPARING|OPEN|PAUSED|CLOSED", message = "狀態值不合法")
    private String status;
}
