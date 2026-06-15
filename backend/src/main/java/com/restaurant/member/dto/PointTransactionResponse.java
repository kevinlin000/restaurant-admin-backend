package com.restaurant.member.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PointTransactionResponse {
    private Long txId;
    private Integer pointChange;
    private String transactionType;
    private String storeName; // 從 Store join 過來
    private LocalDateTime createdAt;
}
