package com.restaurant.faq.dto;

import com.restaurant.faq.entity.FaqCategory;
import com.restaurant.faq.entity.FaqStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FaqItemRequest {

    @NotNull(message = "FAQ 分類不可空白")
    private FaqCategory category;

    @NotNull(message = "FAQ 狀態不可空白")
    private FaqStatus status;

    @NotBlank(message = "問題不可空白")
    @Size(max = 255, message = "問題不可超過 255 字")
    private String question;

    @NotBlank(message = "回答不可空白")
    @Size(max = 1200, message = "回答不可超過 1200 字")
    private String answer;

    @Size(max = 1000, message = "關鍵字不可超過 1000 字")
    private String keywords;

    private Boolean isFeatured;

    private Integer sortOrder;
}
