package com.restaurant.order.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.order.service.LinePayService;
import com.restaurant.order.service.PaymentService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final LinePayService linePayService;

    PaymentController(
            PaymentService paymentService,
            LinePayService linePayService) {
        this.paymentService = paymentService;
        this.linePayService = linePayService;
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

    @PostMapping("/ecpay/result")
    public void ecpayResult(
            @RequestParam Map<String, String> params,
            HttpServletResponse response) throws IOException {

        Long orderId = paymentService.completeEcpayResult(params);

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(
                paymentService.buildPaymentSuccessHtml(orderId));
    }

    @GetMapping("/ecpay/result")
    public void ecpayResultGet(
            @RequestParam Map<String, String> params,
            HttpServletResponse response) throws IOException {

        Long orderId = paymentService.completeEcpayResult(params);

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(
                paymentService.buildPaymentSuccessHtml(orderId));
    }

    @PostMapping("/ecpay/callback")
    public String ecpayCallback(
            @RequestParam Map<String, String> params) {

        System.out.println(params);
        paymentService.handleEcpayCallback(params);
        return "1|OK";
    }

    // @GetMapping("/linepay/success/{orderId}")
    // public void linePaySuccess(
    // @PathVariable Long orderId,
    // HttpServletResponse response) throws IOException {

    // paymentService.simulatePaymentSuccess(orderId);

    // response.setContentType("text/html;charset=UTF-8");
    // response.getWriter().write(
    // paymentService.buildPaymentSuccessHtml(orderId));
    // }

    // @GetMapping("/linepay/request/{orderId}")
    // public void linePayRequest(
    // @PathVariable Long orderId,
    // HttpServletResponse response) throws IOException {

    // String paymentUrl = linePayService.createPaymentUrl(orderId);
    // response.sendRedirect(paymentUrl);
    // }

    @GetMapping("/linepay/payment-url/{orderId}")
    public Map<String, String> linePayPaymentUrl(@PathVariable Long orderId) {
        String paymentUrl = linePayService.createPaymentUrl(orderId);
        return Map.of("paymentUrl", paymentUrl);
    }

    @GetMapping("/linepay/request/{orderId}")
    public void linePayRequest(
            @PathVariable Long orderId,
            HttpServletResponse response) throws IOException {

        String paymentUrl = linePayService.createPaymentUrl(orderId);
        response.sendRedirect(paymentUrl);
    }

    @GetMapping("/linepay/confirm")
    public void linePayConfirm(
            @RequestParam Long orderId,
            @RequestParam String transactionId,
            HttpServletResponse response) throws IOException {

        linePayService.confirmPayment(orderId, transactionId);

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(
                paymentService.buildPaymentSuccessHtml(orderId));
    }
}
