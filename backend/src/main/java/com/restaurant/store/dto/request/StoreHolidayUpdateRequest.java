package com.restaurant.store.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class StoreHolidayUpdateRequest {

    @NotNull(message = "公休日期不可為空")
    private LocalDate holidayDate;

    @Size(max = 100, message = "公休原因最多 100 字")
    private String reason;
}
