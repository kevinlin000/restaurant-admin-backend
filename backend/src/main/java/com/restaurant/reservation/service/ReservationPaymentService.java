package com.restaurant.reservation.service;

import com.restaurant.common.BusinessException;
import com.restaurant.common.ResourceNotFoundException;
import com.restaurant.reservation.dto.ReservationResponse;
import com.restaurant.reservation.entity.Reservation;
import com.restaurant.reservation.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
public class ReservationPaymentService {

    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    private final ReservationEmailService reservationEmailService;
    private final String ecpayHashKey;
    private final String ecpayHashIv;

    public ReservationPaymentService(
            ReservationRepository reservationRepository,
            ReservationService reservationService,
            ReservationEmailService reservationEmailService,
            @Value("${ecpay.hash-key}") String ecpayHashKey,
            @Value("${ecpay.hash-iv}") String ecpayHashIv
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
        this.reservationEmailService = reservationEmailService;
        this.ecpayHashKey = ecpayHashKey;
        this.ecpayHashIv = ecpayHashIv;
    }

    // 依「已建立的訂位」產生綠界付款表單
    public String createEcpayCheckoutForm(Long reservationId) {
        Reservation reservation = findReservation(reservationId);
        validatePayableReservation(reservation);

        Map<String, String> params = new HashMap<>();
        params.put("MerchantID", "3002607");
        params.put("MerchantTradeNo", buildMerchantTradeNo(reservationId));
        params.put("MerchantTradeDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));
        params.put("PaymentType", "aio");
        params.put("TotalAmount", String.valueOf(reservation.getDepositAmount().intValue()));
        params.put("TradeDesc", "Restaurant Reservation Deposit");
        params.put("ItemName", "Reservation Deposit");
        params.put("ReturnURL", "http://localhost:8080/api/reservation-payments/ecpay/callback");
        params.put("OrderResultURL", "http://localhost:8080/api/reservation-payments/ecpay/result?reservationId=" + reservationId);
        params.put("ChoosePayment", "Credit");
        params.put("EncryptType", "1");
        params.put("CheckMacValue", generateCheckMacValue(params));

        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<form id='ecpayForm' method='post' action='https://payment-stage.ecpay.com.tw/Cashier/AioCheckOut/V5'>");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            html.append("<input type='hidden' name='")
                    .append(entry.getKey())
                    .append("' value='")
                    .append(entry.getValue())
                    .append("'/>");
        }
        html.append("</form>");
        html.append("<script>document.getElementById('ecpayForm').submit();</script>");
        html.append("</body></html>");
        return html.toString();
    }

    // 綠界 callback 成功後，訂位更新為已付款，然後寄正式訂位成功信
    @Transactional
    public ReservationResponse completePayment(Long reservationId) {
        Reservation reservation = findReservation(reservationId);
        if ("PAID".equals(reservation.getPaymentStatus())) {
            return reservationService.toResponse(reservation);
        }
        validatePayableReservation(reservation);

        reservation.setPaymentStatus("PAID");
        Reservation saved = reservationRepository.save(reservation);
        ReservationResponse response = reservationService.toResponse(saved);
        sendReservationCreatedEmailAfterCommit(response);
        return response;
    }

    // ＊綠界 Server-to-server callback；RtnCode=1 才代表付款成功＊
    @Transactional
    public void handleEcpayCallback(Map<String, String> params) {
        verifyEcpayCheckMacValue(params);
        String rtnCode = params.get("RtnCode");
        String merchantTradeNo = params.get("MerchantTradeNo");
        if (!"1".equals(rtnCode)) {
            throw new BusinessException("訂金付款失敗：" + params.get("RtnMsg"));
        }
        completePayment(parseReservationId(merchantTradeNo));
    }

    // ＊綠界前端返回頁，結果就同步更新，最後導回訂位成功頁＊
    @Transactional
    public String buildPaymentResultHtml(Long reservationId, Map<String, String> params) {
        if ("1".equals(params.get("RtnCode")) && params.get("MerchantTradeNo") != null) {
            handleEcpayCallback(params);
        }
        ReservationResponse reservation = reservationService.getReservation(reservationId);
        return buildRedirectHtml(reservation);
    }

    private String buildRedirectHtml(ReservationResponse reservation) {
        StringBuilder successUrl = new StringBuilder("http://localhost:5173/reservation-success?id=")
                .append(reservation.getReservationId());
        if (reservation.getAccessToken() != null && !reservation.getAccessToken().isBlank()) {
            successUrl.append("&token=").append(reservation.getAccessToken());
        }
        if ("PAID".equals(reservation.getPaymentStatus())) {
            successUrl.append("&paid=1");
        }
        return """
                <!DOCTYPE html>
                <html lang="zh-TW">
                <head>
                    <meta charset="UTF-8">
                    <title>訂金付款結果</title>
                    <script>window.location.replace("{{SUCCESS_URL}}");</script>
                </head>
                <body>
                    <p>正在返回訂位頁面...</p>
                    <a href="{{SUCCESS_URL}}">若未自動跳轉，請點此返回</a>
                </body>
                </html>
                """.replace("{{SUCCESS_URL}}", successUrl.toString());
    }

    private Reservation findReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("訂位", reservationId));
    }

    private void validatePayableReservation(Reservation reservation) {
        if (!hasDeposit(reservation)) {
            throw new BusinessException("此訂位不需要支付訂金");
        }
        if ("PAID".equals(reservation.getPaymentStatus())) {
            throw new BusinessException("此訂位已完成訂金付款");
        }
        if ("CANCELLED".equals(reservation.getStatus())) {
            throw new BusinessException("此訂位已取消，無法付款");
        }
        if (reservation.getCreatedAt() != null && reservation.getCreatedAt().isBefore(LocalDateTime.now().minusHours(1))) {
            reservationService.cancelExpiredUnpaidDepositReservationNow(reservation.getReservationId());
            throw new BusinessException("訂金付款期限已超過 1 小時，訂位已自動取消");
        }
    }

    private boolean hasDeposit(Reservation reservation) {
        BigDecimal amount = reservation.getDepositAmount() == null ? BigDecimal.ZERO : reservation.getDepositAmount();
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    private void sendReservationCreatedEmailAfterCommit(ReservationResponse response) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            reservationEmailService.sendReservationCreatedEmail(response);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                reservationEmailService.sendReservationCreatedEmail(response);
            }
        });
    }

    private String buildMerchantTradeNo(Long reservationId) {
        return "RS" + reservationId + "T" + System.currentTimeMillis() % 1000000;
    }

    private Long parseReservationId(String merchantTradeNo) {
        if (merchantTradeNo == null || !merchantTradeNo.startsWith("RS") || !merchantTradeNo.contains("T")) {
            throw new BusinessException("訂金付款交易編號格式錯誤");
        }
        return Long.valueOf(merchantTradeNo.substring(2, merchantTradeNo.indexOf("T")));
    }

    private String generateCheckMacValue(Map<String, String> params) {
        Map<String, String> sortedMap = new TreeMap<>(params);
        StringBuilder raw = new StringBuilder("HashKey=").append(ecpayHashKey);
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            raw.append("&").append(entry.getKey()).append("=").append(entry.getValue());
        }
        raw.append("&HashIV=").append(ecpayHashIv);

        try {
            String encoded = URLEncoder.encode(raw.toString(), StandardCharsets.UTF_8).toLowerCase();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(encoded.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                result.append(String.format("%02X", b));
            }
            return result.toString();
        } catch (Exception e) {
            throw new BusinessException("產生訂金付款驗證碼失敗");
        }
    }

    private void verifyEcpayCheckMacValue(Map<String, String> params) {
        String received = params.get("CheckMacValue");
        if (received == null || received.isBlank()) {
            throw new BusinessException("訂金付款驗證碼缺失");
        }

        Map<String, String> valuesToVerify = new HashMap<>(params);
        valuesToVerify.remove("CheckMacValue");
        valuesToVerify.remove("reservationId");
        String expected = generateCheckMacValue(valuesToVerify);
        if (!expected.equalsIgnoreCase(received)) {
            throw new BusinessException("訂金付款驗證碼不符");
        }
    }
}
