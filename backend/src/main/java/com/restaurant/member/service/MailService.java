package com.restaurant.member.service;

public interface MailService {

    void sendEmail(
            String to,
            String subject,
            String content);
}