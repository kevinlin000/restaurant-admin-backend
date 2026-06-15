package com.restaurant.store.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StoreImageCreateRequest {

    @NotBlank(message = "圖片 URL 不可為空")
    @Size(max = 500, message = "圖片 URL 最多 500 字")
    private String imageUrl;

    @Size(max = 100, message = "圖說最多 100 字")
    private String caption;

    @Min(value = 0, message = "排序不可小於 0")
    private Integer sortOrder = 0;
}
