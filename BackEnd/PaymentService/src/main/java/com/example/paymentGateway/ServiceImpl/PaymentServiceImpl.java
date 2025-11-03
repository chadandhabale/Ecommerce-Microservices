package com.example.paymentGateway.ServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.paymentGateway.DTO.PaymentRequestDTO;
import com.example.paymentGateway.DTO.PaymentResponseDTO;
import com.example.paymentGateway.DTO.PaymentStatisticsDTO;
import com.example.paymentGateway.DTO.PaymentVerificationDTO;
import com.example.paymentGateway.Entity.Payment;
import com.example.paymentGateway.Entity.PaymentStatus;
import com.example.paymentGateway.Repository.PaymentRepository;
import com.example.paymentGateway.Service.EmailService;
import com.example.paymentGateway.Service.PaymentService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final EmailService emailService;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Value("${payment.mock:true}")
    private boolean paymentMock;

    // =========================================
    // 🟢 CREATE PAYMENT - Razorpay Order Creation
    // =========================================
    @Override
    public PaymentResponseDTO createPayment(PaymentRequestDTO paymentRequest) {
        try {
            log.info("🔄 Creating payment order for user: {}, amount: {}", 
                    paymentRequest.getUserId(), paymentRequest.getAmount());

            // Create Razorpay order
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", (int) (paymentRequest.getAmount() * 100)); // Convert to paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "txn_" + UUID.randomUUID().toString().substring(0, 8));
            orderRequest.put("payment_capture", 1);

            // Add customer details as notes
            JSONObject notes = new JSONObject();
            if (paymentRequest.getCustomerEmail() != null) {
                notes.put("customer_email", paymentRequest.getCustomerEmail());
            }
            if (paymentRequest.getCustomerName() != null) {
                notes.put("customer_name", paymentRequest.getCustomerName());
            }
            if (paymentRequest.getDescription() != null) {
                notes.put("description", paymentRequest.getDescription());
            }
            orderRequest.put("notes", notes);

            Order razorpayOrder = client.orders.create(orderRequest);

            // Save payment in database
            Payment payment = Payment.builder()
                    .razorpayOrderId(razorpayOrder.get("id"))
                    .amount(paymentRequest.getAmount())
                    .currency("INR")
                    .status(PaymentStatus.PENDING)
                    .userId(paymentRequest.getUserId())
                    .orderId(paymentRequest.getOrderId())
                    .customerEmail(paymentRequest.getCustomerEmail())
                    .customerName(paymentRequest.getCustomerName())
                    .description(paymentRequest.getDescription())
                    .receipt(razorpayOrder.get("receipt"))
                    .build();

            Payment savedPayment = paymentRepository.save(payment);
            log.info("✅ Payment order created successfully: {}", savedPayment.getRazorpayOrderId());

            return new PaymentResponseDTO(
                    razorpayOrder.get("id"),
                    paymentRequest.getAmount(),
                    "INR",
                    "PENDING",
                    paymentRequest.getOrderId(),
                    paymentRequest.getUserId(),
                    razorpayKeyId,
                    paymentRequest.getCustomerEmail()
            );

        } catch (Exception e) {
            log.error("❌ Failed to create payment order: {}", e.getMessage(), e);
            throw new RuntimeException("Payment creation failed: " + e.getMessage());
        }
    }

    // =========================================
    // 🟢 VERIFY PAYMENT - Razorpay Signature Verification
    // =========================================
    @Override
    public boolean verifyPayment(PaymentVerificationDTO verificationDTO) {
        try {
            log.info("🔄 Verifying payment for order: {}", verificationDTO.getRazorpayOrderId());

            boolean isValid;
            
            // ✅ MOCK MODE: Bypass verification for testing
            if (paymentMock) {
                log.info("🟡 MOCK MODE: Bypassing Razorpay signature verification");
                isValid = true;
            } else {
                // Real Razorpay verification
                JSONObject attributes = new JSONObject();
                attributes.put("razorpay_order_id", verificationDTO.getRazorpayOrderId());
                attributes.put("razorpay_payment_id", verificationDTO.getRazorpayPaymentId());
                attributes.put("razorpay_signature", verificationDTO.getRazorpaySignature());

                isValid = Utils.verifyPaymentSignature(attributes, razorpayKeySecret);
            }

            if (isValid) {
                Payment payment = paymentRepository.findByRazorpayOrderId(verificationDTO.getRazorpayOrderId())
                        .orElseThrow(() -> new RuntimeException("Payment not found"));

                payment.setRazorpayPaymentId(verificationDTO.getRazorpayPaymentId());
                payment.setRazorpaySignature(verificationDTO.getRazorpaySignature());
                payment.setStatus(PaymentStatus.SUCCESS);
                
                Payment updatedPayment = paymentRepository.save(payment);

                // Send success email
                if (updatedPayment.getCustomerEmail() != null && !updatedPayment.getCustomerEmail().isBlank()) {
                    String customerName = updatedPayment.getCustomerName() != null ? 
                            updatedPayment.getCustomerName() : "Customer";
                    
                    emailService.sendPaymentSuccessEmail(
                            updatedPayment.getCustomerEmail(),
                            customerName,
                            updatedPayment.getAmount(),
                            updatedPayment.getOrderId() != null ? updatedPayment.getOrderId() : updatedPayment.getId()
                    );
                }

                log.info("✅ Payment verified successfully: {}", verificationDTO.getRazorpayOrderId());
            } else {
                log.warn("❌ Payment verification failed: {}", verificationDTO.getRazorpayOrderId());
                
                // Update payment status to failed
                paymentRepository.findByRazorpayOrderId(verificationDTO.getRazorpayOrderId())
                    .ifPresent(payment -> {
                        payment.setStatus(PaymentStatus.FAILED);
                        paymentRepository.save(payment);
                    });
            }

            return isValid;

        } catch (Exception e) {
            log.error("❌ Payment verification error: {}", e.getMessage(), e);
            return false;
        }
    }

    // =========================================
    // 🟢 UPDATE PAYMENT STATUS
    // =========================================
    @Override
    public PaymentResponseDTO updatePaymentStatus(String razorpayOrderId, PaymentStatus status) {
        try {
            log.info("🔄 Updating payment status for: {} to {}", razorpayOrderId, status);
            
            Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                    .orElseThrow(() -> new RuntimeException("Payment not found: " + razorpayOrderId));

            payment.setStatus(status);
            Payment updatedPayment = paymentRepository.save(payment);
            
            log.info("✅ Payment status updated successfully: {} -> {}", razorpayOrderId, status);

            // Convert to DTO and return
            return new PaymentResponseDTO(
                updatedPayment.getRazorpayOrderId(),
                updatedPayment.getAmount(),
                updatedPayment.getCurrency(),
                updatedPayment.getStatus().toString(),
                updatedPayment.getOrderId(),
                updatedPayment.getUserId(),
                razorpayKeyId,
                updatedPayment.getCustomerEmail()
            );

        } catch (Exception e) {
            log.error("❌ Failed to update payment status: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update payment status: " + e.getMessage());
        }
    }

    // =========================================
    // 🟢 GET PAYMENT BY ORDER ID
    // =========================================
    @Override
    public Payment getPaymentByOrderId(String razorpayOrderId) {
        return paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + razorpayOrderId));
    }

    // =========================================
    // 🟢 GET PAYMENTS BY USER
    // =========================================
    @Override
    public List<Payment> getPaymentsByUser(Long userId) {
        return paymentRepository.findByUserId(userId);
    }

    // =========================================
    // 🟢 GET PAYMENTS BY STATUS
    // =========================================
    @Override
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    // =========================================
    // 🟢 GET RECENT PAYMENTS
    // =========================================
    @Override
    public List<Payment> getRecentPayments(int days) {
        log.info("🔄 Fetching recent payments from last {} days", days);
        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(days);
            LocalDateTime endDate = LocalDateTime.now();
            return paymentRepository.findPaymentsBetweenDates(startDate, endDate);
        } catch (Exception e) {
            log.error("❌ Failed to fetch recent payments: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch recent payments: " + e.getMessage());
        }
    }

    // =========================================
    // 🟢 GET PAYMENT STATISTICS
    // =========================================
    @Override
    public PaymentStatisticsDTO getPaymentStatistics() {
        try {
            Long totalPayments = paymentRepository.count();
            Long successfulPayments = paymentRepository.countByStatus(PaymentStatus.SUCCESS);
            Long pendingPayments = paymentRepository.countByStatus(PaymentStatus.PENDING);
            Long failedPayments = paymentRepository.countByStatus(PaymentStatus.FAILED);
            
            Double totalRevenue = paymentRepository.getTotalSuccessfulAmount();
            if (totalRevenue == null) totalRevenue = 0.0;
            
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
            Double todayRevenue = paymentRepository.getRevenueBetweenDates(startOfDay, endOfDay);
            if (todayRevenue == null) todayRevenue = 0.0;

            log.info("📊 Payment statistics - Total: {}, Success: {}, Revenue: {}", 
                    totalPayments, successfulPayments, totalRevenue);

            return new PaymentStatisticsDTO(
                    totalPayments,
                    successfulPayments,
                    pendingPayments,
                    failedPayments,
                    totalRevenue,
                    todayRevenue
            );
        } catch (Exception e) {
            log.error("❌ Failed to get payment statistics: {}", e.getMessage());
            throw new RuntimeException("Failed to get payment statistics: " + e.getMessage());
        }
    }

    // =========================================
    // 🟢 REFUND PAYMENT (Not Implemented)
    // =========================================
    @Override
    public Payment refundPayment(String razorpayOrderId, Double amount) {
        log.info("🔄 Refund requested for order: {}, amount: {}", razorpayOrderId, amount);
        throw new UnsupportedOperationException("Refund functionality not implemented yet");
    }

    // =========================================
    // 🟢 CHECK IF PAYMENT IS VALID
    // =========================================
    @Override
    public boolean isPaymentValid(String razorpayOrderId) {
        return paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .map(payment -> payment.getStatus() == PaymentStatus.SUCCESS)
                .orElse(false);
    }
}