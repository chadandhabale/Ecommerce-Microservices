package com.example.paymentGateway.Service;

import java.util.List;

import com.example.paymentGateway.DTO.PaymentRequestDTO;
import com.example.paymentGateway.DTO.PaymentResponseDTO;
import com.example.paymentGateway.DTO.PaymentStatisticsDTO;
import com.example.paymentGateway.DTO.PaymentVerificationDTO;
import com.example.paymentGateway.Entity.Payment;
import com.example.paymentGateway.Entity.PaymentStatus;

public interface PaymentService {
    
    // Core payment operations
    PaymentResponseDTO createPayment(PaymentRequestDTO paymentRequest);
    boolean verifyPayment(PaymentVerificationDTO verificationDTO);
    PaymentResponseDTO updatePaymentStatus(String razorpayOrderId, PaymentStatus status); // ✅ MUST RETURN DTO
    
    // Query operations (these can still return entities since they're internal)
    Payment getPaymentByOrderId(String razorpayOrderId);
    List<Payment> getPaymentsByUser(Long userId);
    List<Payment> getPaymentsByStatus(PaymentStatus status);
    List<Payment> getRecentPayments(int days);
    
    // Admin operations
    PaymentStatisticsDTO getPaymentStatistics();
    Payment refundPayment(String razorpayOrderId, Double amount);
    
    // Utility operations
    boolean isPaymentValid(String razorpayOrderId);
}