package com.restaurant.store.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NearbySearchRequest {

    @NotNull(message = "緯度不可為空")
    private Double latitude;

    @NotNull(message = "經度不可為空")
    private Double longitude;

    @Min(1) @Max(20)
    private int limit = 5;
}
