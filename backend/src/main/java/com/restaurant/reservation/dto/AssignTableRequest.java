package com.restaurant.reservation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AssignTableRequest {

    @NotNull(message = "訂位 ID 必填")
    private Long reservationId;

    @NotNull(message = "桌位 ID 必填")
    private List<Long> tableIds;
}
