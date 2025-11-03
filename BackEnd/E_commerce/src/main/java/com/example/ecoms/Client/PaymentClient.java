package com.example.ecoms.Client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.example.ecoms.DTO.PaymentRequestDTO;
import com.example.ecoms.DTO.PaymentResponseDTO;

@FeignClient(
    name = "PaymentService",
    configuration = com.example.ecoms.Config.FeignConfig.class,
    path = "/api/payments"
)
public interface PaymentClient {
    
    @PostMapping("/create")
    PaymentResponseDTO createPayment(@RequestBody PaymentRequestDTO paymentRequest);

    @PutMapping("/update-status/{razorpayOrderId}")
    PaymentResponseDTO updatePaymentStatus(
        @PathVariable("razorpayOrderId") String razorpayOrderId,
        @RequestParam("status") String status
    );
    
    @PostMapping("/verify")
    Object verifyPayment(@RequestBody Object verificationDTO);

    // ✅ ADD TEST ENDPOINT
    @GetMapping("/feign-test")
    String feignTest();
}