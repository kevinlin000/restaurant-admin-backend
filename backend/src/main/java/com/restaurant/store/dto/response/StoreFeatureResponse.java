package com.restaurant.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreFeatureResponse {
    private Long featureId;
    private String featureKey;
    private String featureLabel;
    private Integer sortOrder;
}
