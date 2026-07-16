package com.restaurant.reservation.controller;

import com.restaurant.reservation.service.ReservationPaymentService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/reservation-payments")
@RequiredArgsConstructor
public class ReservationPaymentController {

    private final ReservationPaymentService reservationPaymentService;

    // 顧客訂位後如果需訂金，用 reservationId 產生綠界付款表單
    @GetMapping(value = "/ecpay/checkout/{reservationId}", produces = MediaType.TEXT_HTML_VALUE)
    public String checkout(@PathVariable Long reservationId) {
        return reservationPaymentService.createEcpayCheckoutForm(reservationId);
    }

    // 綠界付款完成後導回這裡，同步更新付款狀態，再回成功頁
    @PostMapping("/ecpay/result")
    public void ecpayResultPost(
            @RequestParam Map<String, String> params,
            HttpServletResponse response
    ) throws IOException {
        writeResultPage(params, response);
    }

    // ＊測試環境用 GET 開結果頁＊
    @GetMapping("/ecpay/result")
    public void ecpayResultGet(
            @RequestParam Map<String, String> params,
            HttpServletResponse response
    ) throws IOException {
        writeResultPage(params, response);
    }

    // 綠界後端通知；收到付款成功 callback 把訂位改成已付款
    @PostMapping("/ecpay/callback")
    public String ecpayCallback(@RequestParam Map<String, String> params) {
        reservationPaymentService.handleEcpayCallback(params);
        return "1|OK";
    }

    private void writeResultPage(Map<String, String> params, HttpServletResponse response) throws IOException {
        Long reservationId = Long.valueOf(params.get("reservationId"));
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(reservationPaymentService.buildPaymentResultHtml(reservationId, params));
    }
}
