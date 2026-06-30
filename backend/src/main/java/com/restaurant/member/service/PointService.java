package com.restaurant.member.service;

import java.math.BigDecimal;
import java.util.List;

import com.restaurant.member.dto.PointBalanceResponse;
import com.restaurant.member.dto.PointTransactionResponse;

public interface PointService {

    /**
     * 查詢會員目前點數與等級資訊。
     * pointBalance：可用折抵點數。
     * pointLevel：會員升級點數，不因折抵減少。
     */
    PointBalanceResponse getPointBalance(Long userId);

    /**
     * 查詢會員點數異動紀錄。
     */
    List<PointTransactionResponse> getPointHistory(Long userId);

    /**
     * 訂單完成後累積點數。
     * 規則：每消費 100 元累積 1 點。
     * 本方法會同時增加 pointBalance 與 pointLevel。
     *
     * @return 本次實際累積點數；未登入或金額不足時回傳 0。
     */
    int earnPointsFromOrder(Long userId, Long storeId, Long orderId, BigDecimal totalAmount);

    /**
     * 訂單使用點數折抵。
     * 本方法只扣 pointBalance，不扣 pointLevel。
     *
     * @return 本次實際扣除點數；未登入或使用點數為 0 時回傳 0。
     */
    int usePointsForOrder(Long userId, Long storeId, Long orderId, Integer pointsToUse);

    /**
     * 依照 pointLevel 更新會員等級。
     */
    void updateMemberLevel(Long userId);

    /**
     * 取消訂單時退回可用折抵點數，只加回 pointBalance，不增加 pointLevel。
     */
    int refundPointsForOrder(Long userId, Long storeId, Long orderId, Integer pointsToRefund);
}
