package com.restaurant.store.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TableUpdateRequest {

    @Size(max = 10)
    private String tableNumber;

    @Min(value = 1, message = "座位數至少為 1")
    private Integer tableSize;

    @Size(max = 20)
    private String tableType;

    @Size(max = 20)
    private String zone;

    @Pattern(regexp = "AVAILABLE|DISABLED|MAINTENANCE", message = "狀態值不合法")
    private String status;

    private Boolean isCombinable;
}
