package com.restaurant.order.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.order.entity.Order;
import com.restaurant.order.repository.OrderRepository;

@Service
public class LinePayService {

    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${linepay.channel-id}")
    private String channelId;

    @Value("${linepay.channel-secret}")
    private String channelSecret;

    @Value("${linepay.api-base-url}")
    private String linePayApiBaseUrl;

    @Value("${linepay.confirm-url}")
    private String confirmUrl;

    @Value("${linepay.cancel-url}")
    private String cancelUrl;

    public LinePayService(
            OrderRepository orderRepository,
            PaymentService paymentService,
            ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    public String createPaymentUrl(Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("找不到訂單"));

            int amount = order.getFinalAmount().setScale(0, BigDecimal.ROUND_HALF_UP).intValue();

            String uri = "/v3/payments/request";

            Map<String, Object> product = new LinkedHashMap<>();
            product.put("name", "Restaurant Meal");
            product.put("quantity", 1);
            product.put("price", amount);

            Map<String, Object> packageInfo = new LinkedHashMap<>();
            packageInfo.put("id", "PKG-" + orderId);
            packageInfo.put("amount", amount);
            packageInfo.put("name", "Restaurant Order");
            packageInfo.put("products", java.util.List.of(product));

            Map<String, Object> redirectUrls = new LinkedHashMap<>();
            redirectUrls.put("confirmUrl", confirmUrl + "?orderId=" + orderId);
            redirectUrls.put("cancelUrl", cancelUrl);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("amount", amount);
            body.put("currency", "TWD");
            body.put("orderId", "ORDER-" + orderId + "-" + System.currentTimeMillis());
            body.put("packages", java.util.List.of(packageInfo));
            body.put("redirectUrls", redirectUrls);

            String requestBody = objectMapper.writeValueAsString(body);
            String nonce = UUID.randomUUID().toString();
            String signature = createSignature(uri, requestBody, nonce);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-LINE-ChannelId", channelId);
            headers.set("X-LINE-Authorization-Nonce", nonce);
            headers.set("X-LINE-Authorization", signature);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    linePayApiBaseUrl + uri,
                    entity,
                    Map.class);

            Map<String, Object> responseBody = response.getBody();

            if (responseBody == null) {
                throw new RuntimeException("LINE Pay 回應為空");
            }

            String returnCode = String.valueOf(responseBody.get("returnCode"));

            if (!"0000".equals(returnCode)) {
                throw new RuntimeException("LINE Pay Request 失敗：" + responseBody);
            }

            Map<String, Object> info = (Map<String, Object>) responseBody.get("info");
            Map<String, Object> paymentUrl = (Map<String, Object>) info.get("paymentUrl");

        

            return String.valueOf(paymentUrl.get("web"));

        } catch (Exception e) {
            throw new RuntimeException("建立 LINE Pay 付款失敗：" + e.getMessage(), e);
        }
    }

    public void confirmPayment(Long orderId, String transactionId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("找不到訂單"));

            int amount = order.getFinalAmount().setScale(0, BigDecimal.ROUND_HALF_UP).intValue();

            String uri = "/v3/payments/" + transactionId + "/confirm";

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("amount", amount);
            body.put("currency", "TWD");

            String requestBody = objectMapper.writeValueAsString(body);
            String nonce = UUID.randomUUID().toString();
            String signature = createSignature(uri, requestBody, nonce);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-LINE-ChannelId", channelId);
            headers.set("X-LINE-Authorization-Nonce", nonce);
            headers.set("X-LINE-Authorization", signature);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    linePayApiBaseUrl + uri,
                    entity,
                    Map.class);

            Map<String, Object> responseBody = response.getBody();

            if (responseBody == null) {
                throw new RuntimeException("LINE Pay Confirm 回應為空");
            }

            String returnCode = String.valueOf(responseBody.get("returnCode"));

            if (!"0000".equals(returnCode)) {
                throw new RuntimeException("LINE Pay Confirm 失敗：" + responseBody);
            }

            paymentService.simulatePaymentSuccess(orderId);

        } catch (Exception e) {
            throw new RuntimeException("LINE Pay 確認付款失敗：" + e.getMessage(), e);
        }
    }

    private String createSignature(String uri, String requestBody, String nonce) throws Exception {
        String message = channelSecret + uri + requestBody + nonce;

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                channelSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256");

        mac.init(secretKeySpec);

        byte[] hmacBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(hmacBytes);
    }
}