package com.restaurant.store.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NearbySearchRequest {

    @NotNull(message = "緯度不可為空")
    @DecimalMin(value = "-90.0", message = "緯度不可小於 -90")
    @DecimalMax(value = "90.0", message = "緯度不可大於 90")
    private Double latitude;

    @NotNull(message = "經度不可為空")
    @DecimalMin(value = "-180.0", message = "經度不可小於 -180")
    @DecimalMax(value = "180.0", message = "經度不可大於 180")
    private Double longitude;

    @Min(1) @Max(20)
    private int limit = 5;
}
