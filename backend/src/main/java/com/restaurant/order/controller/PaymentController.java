package com.restaurant.order.controller;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.order.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{orderId}/success")
    public String simulatePaymentSuccess(@PathVariable Long orderId) {
        paymentService.simulatePaymentSuccess(orderId);
        return "付款成功，訂單狀態已更新";
    }

    @GetMapping(value = "/ecpay/checkout/{orderId}", produces = MediaType.TEXT_HTML_VALUE)
    public String checkout(@PathVariable Long orderId) {
        return paymentService.createEcpayCheckoutForm(orderId);
    }

    @GetMapping(value = "/ecpay/result", produces = MediaType.TEXT_HTML_VALUE)
    public String ecpayResultGet(@RequestParam Long orderId) {
        return ecpayResult(orderId);
    }

    @PostMapping(value = "/ecpay/result", produces = MediaType.TEXT_HTML_VALUE)
    public String ecpayResult(@RequestParam Long orderId) {

        paymentService.simulatePaymentSuccess(orderId);

        return "<html><body>"
                + "<h1>付款成功</h1>"
                + "<p>訂單編號：" + orderId + "</p>"
                + "<a href='http://localhost:5173/payment-success?orderId=" + orderId + "'>查看訂單結果</a>"
                + "</body></html>";
    }

    @PostMapping("/ecpay/callback")
    public String ecpayCallback(
            @RequestParam Map<String, String> params) {

        System.out.println(params);
        paymentService.handleEcpayCallback(params);
        return "1|OK";
    }

    @GetMapping("/linepay/request/{orderId}")
    public String linePayRequest(@PathVariable Long orderId) {
        return paymentService.createLinePayRequest(orderId);
    }
}
