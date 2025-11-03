package com.example.paymentGateway.Service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
    void sendHtmlEmail(String to, String subject, String htmlContent);
    void sendPaymentSuccessEmail(String to, String customerName, Double amount, Long orderId);
}