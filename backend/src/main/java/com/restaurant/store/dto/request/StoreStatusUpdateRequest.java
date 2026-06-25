package com.restaurant.store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class StoreStatusUpdateRequest {

    @NotBlank(message = "狀態不可為空")
    @Pattern(regexp = "PREPARING|OPEN|PAUSED|CLOSED", message = "狀態值不合法")
    private String status;
}
