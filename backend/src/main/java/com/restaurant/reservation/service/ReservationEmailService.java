package com.restaurant.reservation.service;

import com.restaurant.reservation.dto.ReservationResponse;
import com.restaurant.store.entity.Store;
import com.restaurant.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationEmailService {

    private final JavaMailSender mailSender;
    private final StoreRepository storeRepository;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Value("${app.frontend-base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    // 訂位成功後寄出通知信（寄信失敗只記 log，不會訂位失敗）
    public void sendReservationCreatedEmail(ReservationResponse reservation) {
        if (reservation == null || !StringUtils.hasText(reservation.getCustomerEmail())) {
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (StringUtils.hasText(mailFrom)) {
                message.setFrom(mailFrom);
            }
            message.setTo(reservation.getCustomerEmail());
            message.setSubject("敘日訂位成功通知");
            message.setText(buildReservationCreatedText(reservation));
            mailSender.send(message);
        } catch (Exception error) {
            log.warn("訂位成功信寄送失敗 reservationId={}, email={}",
                    reservation.getReservationId(),
                    reservation.getCustomerEmail(),
                    error);
        }
    }

    // 需訂金的訂位先寄待付款通知；顧客可在一小時內從信件連回訂位頁完成付款。
    public void sendReservationPaymentPendingEmail(ReservationResponse reservation) {
        if (reservation == null || !StringUtils.hasText(reservation.getCustomerEmail())) {
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (StringUtils.hasText(mailFrom)) {
                message.setFrom(mailFrom);
            }
            message.setTo(reservation.getCustomerEmail());
            message.setSubject("敘日訂位待付款通知");
            message.setText(buildReservationPaymentPendingText(reservation));
            mailSender.send(message);
        } catch (Exception error) {
            log.warn("訂位待付款信寄送失敗 reservationId={}, email={}",
                    reservation.getReservationId(),
                    reservation.getCustomerEmail(),
                    error);
        }
    }

    private String buildReservationCreatedText(ReservationResponse reservation) {
        String storeName = storeRepository.findByStoreIdAndIsDeletedFalse(reservation.getStoreId())
                .map(Store::getStoreName)
                .orElse("敘日餐廳");
        String timeRange = "%s - %s".formatted(
                formatTime(reservation.getStartTime()),
                formatTime(reservation.getEndTime())
        );
        String successUrl = buildSuccessUrl(reservation);

        return """
                %s 您好：

                您的訂位已成功送出，以下是本次訂位資訊：

                分店：%s
                日期：%s
                時段：%s
                人數：%s 位
                手機：%s
                備註：%s

                %s

                若需修改、保留或取消訂位，請點擊上方訂位成功頁連結操作。

                敘日餐廳 敬上
                """.formatted(
                defaultText(reservation.getCustomerName(), "顧客"),
                storeName,
                reservation.getReservationDate(),
                timeRange,
                reservation.getPartySize(),
                defaultText(reservation.getCustomerPhone(), "-"),
                defaultText(reservation.getSpecialRequest(), "無"),
                successUrl
        );
    }

    private String buildReservationPaymentPendingText(ReservationResponse reservation) {
        String storeName = storeRepository.findByStoreIdAndIsDeletedFalse(reservation.getStoreId())
                .map(Store::getStoreName)
                .orElse("敘日餐廳");
        String timeRange = "%s - %s".formatted(
                formatTime(reservation.getStartTime()),
                formatTime(reservation.getEndTime())
        );
        String successUrl = buildSuccessUrl(reservation);

        return """
                %s 您好：

                您的訂位已送出，目前尚未完成訂金付款。系統會先保留此時段桌位 1 小時，請於期限內完成付款；逾時未付款會自動取消訂位。

                分店：%s
                日期：%s
                時段：%s
                人數：%s 位
                訂金：%s 元
                手機：%s
                備註：%s

                請點擊下方連結回到訂位頁面完成付款：
                %s

                敘日餐廳 敬上
                """.formatted(
                defaultText(reservation.getCustomerName(), "顧客"),
                storeName,
                reservation.getReservationDate(),
                timeRange,
                reservation.getPartySize(),
                reservation.getDepositAmount().intValue(),
                defaultText(reservation.getCustomerPhone(), "-"),
                defaultText(reservation.getSpecialRequest(), "無"),
                successUrl
        );
    }

    private String buildSuccessUrl(ReservationResponse reservation) {
        if (reservation == null
                || reservation.getReservationId() == null
                || !StringUtils.hasText(reservation.getAccessToken())) {
            return "-";
        }
        String baseUrl = frontendBaseUrl == null ? "" : frontendBaseUrl.replaceAll("/+$", "");
        return "%s/reservation-success?id=%s&token=%s".formatted(
                baseUrl,
                reservation.getReservationId(),
                reservation.getAccessToken()
        );
    }

    private String formatTime(Object time) {
        return time == null ? "-" : time.toString().substring(0, Math.min(5, time.toString().length()));
    }

    private String defaultText(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }
}
