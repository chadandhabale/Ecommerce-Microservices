package com.example.paymentGateway.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.paymentGateway.DTO.*;
import com.example.paymentGateway.Entity.Payment;
import com.example.paymentGateway.Entity.PaymentStatus;
import com.example.paymentGateway.Service.EmailService;
import com.example.paymentGateway.Service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final EmailService emailService;

    // -------------------------------
    // 🟢 Create Payment (Feign or Gateway)
    // -------------------------------
    @PostMapping("/create")
    public PaymentResponseDTO createPayment(@Valid @RequestBody PaymentRequestDTO paymentRequest) {
        log.info("📦 Creating payment for order: {}", paymentRequest.getOrderId());
        return paymentService.createPayment(paymentRequest);
    }

    // -------------------------------
    // 🟢 Verify Payment (Feign or Frontend)
    // -------------------------------
    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(@Valid @RequestBody PaymentVerificationDTO verificationDTO) {
        log.info("🧾 Verifying Razorpay order: {}", verificationDTO.getRazorpayOrderId());
        boolean isValid = paymentService.verifyPayment(verificationDTO);
        return isValid
                ? ResponseEntity.ok("✅ Payment verified successfully")
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("❌ Payment verification failed");
    }

    // -------------------------------
    // 🟢 Update Payment Status (Admin / Feign)
    // -------------------------------
    @PutMapping("/update-status/{razorpayOrderId}")
    public PaymentResponseDTO updatePaymentStatus(
            @PathVariable String razorpayOrderId,
            @RequestParam String status) {
        log.info("🔄 Updating status for {} → {}", razorpayOrderId, status);
        PaymentStatus paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
        return paymentService.updatePaymentStatus(razorpayOrderId, paymentStatus);
    }

    // -------------------------------
    // 🟢 Feign Test Endpoint
    // -------------------------------
    @GetMapping("/feign-test")
    public String feignTest() {
        return "✅ Payment Service Connected - " + System.currentTimeMillis();
    }

    // -------------------------------
    // 🟢 Analytics & History Endpoints
    // -------------------------------
    @GetMapping("/order/{razorpayOrderId}")
    public ResponseEntity<Payment> getPayment(@PathVariable String razorpayOrderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(razorpayOrderId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Payment>> getUserPayments(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentService.getPaymentsByUser(userId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Payment>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        return ResponseEntity.ok(paymentService.getPaymentsByStatus(status));
    }

    @GetMapping("/recent/{days}")
    public ResponseEntity<List<Payment>> getRecentPayments(@PathVariable int days) {
        return ResponseEntity.ok(paymentService.getRecentPayments(days));
    }

    @GetMapping("/statistics")
    public ResponseEntity<PaymentStatisticsDTO> getStatistics() {
        return ResponseEntity.ok(paymentService.getPaymentStatistics());
    }

    // -------------------------------
    // 🟢 Test Endpoints (Optional)
    // -------------------------------
    @PostMapping("/test-email")
    public ResponseEntity<String> testEmail(@RequestParam String email) {
        emailService.sendPaymentSuccessEmail(email, "Test User", 99.99, 1001L);
        return ResponseEntity.ok("✅ Test email sent to: " + email);
    }
}
