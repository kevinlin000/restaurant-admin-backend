package com.restaurant.store.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TableCreateRequest {

    @NotBlank(message = "桌號不可為空")
    @Size(max = 10)
    private String tableNumber;

    @NotNull(message = "座位數不可為空")
    @Min(value = 1, message = "座位數至少為 1")
    private Integer tableSize;

    @Size(max = 20)
    private String tableType;

    @Size(max = 20)
    private String zone;

    private Boolean isCombinable = false;
}
