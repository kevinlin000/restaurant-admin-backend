package com.restaurant.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableInfoResponse {
    private Long tableId;
    private String tableNumber;
    private Integer tableSize;
    private String tableType;
    private String zone;
    private String status;
    private Boolean isCombinable;
}
