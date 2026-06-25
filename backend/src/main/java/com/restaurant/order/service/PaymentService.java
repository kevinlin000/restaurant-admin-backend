package com.restaurant.order.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.restaurant.order.entity.Order;
import com.restaurant.order.entity.Payment;
import com.restaurant.order.repository.OrderRepository;
import com.restaurant.order.repository.PaymentRepository;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;

    private final OrderRepository orderRepository;

    PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    public Payment createUnpaidPayment(Order order, String paymentMethod) {
        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(paymentMethod)
                .paymentStatus("UNPAID")
                .build();

        return paymentRepository.save(payment);
    }

    public void simulatePaymentSuccess(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("找不到訂單"));

        Payment payment = paymentRepository.findByOrder(order);

        if (payment == null) {
            throw new RuntimeException("找不到付款資料");
        }

        payment.setPaymentStatus("PAID");
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        order.setStatus("CONFIRMED");
        orderRepository.save(order);

    }

    public String buildPaymentSuccessHtml(Long orderId) {
        String detailUrl = "http://localhost:5173/admin/order-manage?orderId=" + orderId;

        return """
                <!DOCTYPE html>
                <html lang="zh-TW">
                <head>
                    <meta charset="UTF-8">
                    <title>付款成功</title>
                    <style>
                        body {
                            margin: 0;
                            min-height: 100vh;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            background: #f8f3ed;
                            font-family: "Microsoft JhengHei", Arial, sans-serif;
                            color: #344b68;
                        }
                        .card {
                            width: 520px;
                            background: white;
                            border-radius: 24px;
                            padding: 44px 36px;
                            text-align: center;
                            box-shadow: 0 18px 45px rgba(100, 80, 50, 0.15);
                        }
                        .icon {
                            width: 76px;
                            height: 76px;
                            margin: 0 auto 20px;
                            border-radius: 50%;
                            background: #e8f8ef;
                            color: #22a06b;
                            font-size: 42px;
                            line-height: 76px;
                        }
                        h1 {
                            margin: 0 0 12px;
                            font-size: 34px;
                        }
                        p {
                            margin: 10px 0;
                            color: #718096;
                            font-size: 16px;
                        }
                        .order-id {
                            margin: 24px 0;
                            padding: 14px;
                            border-radius: 14px;
                            background: #fff6ef;
                            color: #344b68;
                            font-weight: 700;
                        }
                        a {
                            display: inline-block;
                            margin-top: 12px;
                            padding: 13px 24px;
                            border-radius: 12px;
                            background: #e4a775;
                            color: white;
                            text-decoration: none;
                            font-weight: 700;
                        }
                        a:hover {
                            background: #d9945f;
                        }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <div class="icon">✓</div>
                        <h1>付款成功</h1>
                        <p>付款已完成，訂單已成立。</p>
                        <div class="order-id">訂單編號：#{{ORDER_ID}}</div>
                        <a href="{{DETAIL_URL}}">查看訂單明細</a>
                    </div>
                </body>
                </html>
                """
                .replace("{{ORDER_ID}}", String.valueOf(orderId))
                .replace("{{DETAIL_URL}}", detailUrl);
    }

    public String createEcpayCheckoutForm(Long orderId) {
        // 1. 查訂單
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("找不到訂單"));

        // return "訂單金額：" + order.getFinalAmount();
        // 2. 組綠界參數
        Map<String, String> params = new HashMap<>();
        params.put("MerchantID", "3002607");
        String tradeNo = "OD" + orderId + "T" + System.currentTimeMillis() % 1000000;
        params.put("MerchantTradeNo", tradeNo);
        params.put("MerchantTradeDate",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));

        params.put("PaymentType", "aio");

        params.put("TotalAmount",
                String.valueOf(order.getFinalAmount().intValue()));

        params.put("TradeDesc", "Restaurant Order");
        params.put("ItemName", "Restaurant Meal");

        params.put("ReturnURL",
                "http://localhost:8080/api/payments/ecpay/callback");

        params.put("OrderResultURL",
                "http://localhost:8080/api/payments/ecpay/result?orderId=" + orderId);

        params.put("ChoosePayment", "Credit");

        params.put("EncryptType", "1");
        // 3. 算 CheckMacValue
        String checkMacValue = generateCheckMacValue(params);

        params.put("CheckMacValue", checkMacValue);
        // 4. 回傳 HTML form 自動 submit
        StringBuilder html = new StringBuilder();

        html.append("<html><body>");
        html.append(
                "<form id='ecpayForm' method='post' action='https://payment-stage.ecpay.com.tw/Cashier/AioCheckOut/V5'>");

        for (Map.Entry<String, String> entry : params.entrySet()) {
            html.append("<input type='hidden' name='")
                    .append(entry.getKey())
                    .append("' value='")
                    .append(entry.getValue())
                    .append("'/>");
        }

        html.append("<script>document.getElementById('ecpayForm').submit();</script>");

        html.append("</form>");
        html.append("</body></html>");

        return html.toString();

    }

    private String generateCheckMacValue(Map<String, String> params) {

        String hashKey = "pwFHCqoQZGmho4w6";
        String hashIV = "EkRm7iFT261dpevs";

        Map<String, String> sortedMap = new TreeMap<>(params);

        StringBuilder sb = new StringBuilder();

        sb.append("HashKey=").append(hashKey);

        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            sb.append("&")
                    .append(entry.getKey())
                    .append("=")
                    .append(entry.getValue());
        }

        sb.append("&HashIV=").append(hashIV);

        try {
            String encoded = URLEncoder.encode(
                    sb.toString(),
                    StandardCharsets.UTF_8).toLowerCase();

            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] digest = md.digest(encoded.getBytes(StandardCharsets.UTF_8));

            StringBuilder result = new StringBuilder();

            for (byte b : digest) {
                result.append(
                        String.format("%02X", b));
            }

            return result.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void handleEcpayCallback(Map<String, String> params) {

        String rtnCode = params.get("RtnCode");
        String merchantTradeNo = params.get("MerchantTradeNo");

        if (!"1".equals(rtnCode)) {
            throw new RuntimeException("付款失敗：" + params.get("RtnMsg"));
        }

        String orderIdText = merchantTradeNo
                .substring(2, merchantTradeNo.indexOf("T"));

        Long orderId = Long.valueOf(orderIdText);

        simulatePaymentSuccess(orderId);
    }

    // public String createLinePayRequest(Long orderId) {
    //     orderRepository.findById(orderId)
    //             .orElseThrow(() -> new RuntimeException("找不到訂單"));

    //     return "http://localhost:8080/api/payments/linepay/success/" + orderId;
    // }
}
