package com.restaurant.store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StoreFeatureUpdateRequest {

    @NotBlank(message = "標籤代碼不可為空")
    @Size(max = 40, message = "標籤代碼不可超過 40 字")
    private String featureKey;

    @NotBlank(message = "標籤名稱不可為空")
    @Size(max = 30, message = "標籤名稱不可超過 30 字")
    private String featureLabel;

    private Integer sortOrder = 0;
}
