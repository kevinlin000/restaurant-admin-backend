package com.restaurant.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreImageResponse {
    private Long imageId;
    private String imageUrl;
    private String caption;
    private Integer sortOrder;
}
