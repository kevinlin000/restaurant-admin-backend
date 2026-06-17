package com.restaurant.member.service;

import java.math.BigDecimal;
import java.util.List;

import com.restaurant.member.dto.PointBalanceResponse;
import com.restaurant.member.dto.PointTransactionResponse;

public interface PointService {

    /**
     * 查詢會員目前點數與等級資訊。
     */
    PointBalanceResponse getPointBalance(Long userId);

    /**
     * 查詢會員點數異動紀錄。
     */
    List<PointTransactionResponse> getPointHistory(Long userId);

    /**
     * 訂單完成後累積點數。
     * 規則：每消費 100 元累積 1 點。
     *
     * @return 本次實際累積點數；未登入或金額不足時回傳 0。
     */
    int earnPointsFromOrder(Long userId, Long storeId, Long orderId, BigDecimal totalAmount);

    /**
     * 訂單使用點數折抵。
     *
     * @return 本次實際扣除點數；未登入或使用點數為 0 時回傳 0。
     */
    int usePointsForOrder(Long userId, Long storeId, Long orderId, Integer pointsToUse);

    /**
     * 依照目前點數更新會員等級。
     */
    void updateMemberLevel(Long userId);
}
