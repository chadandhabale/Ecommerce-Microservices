package com.example.paymentGateway.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.paymentGateway.Entity.Payment;
import com.example.paymentGateway.Entity.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
    
    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);
    
    List<Payment> findByUserId(Long userId);
    
    List<Payment> findByOrderId(Long orderId);
    
    List<Payment> findByStatus(PaymentStatus status);
    
    List<Payment> findByCustomerEmail(String customerEmail);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    Long countByStatus(@Param("status") PaymentStatus status);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'SUCCESS'")
    Double getTotalSuccessfulAmount();
    
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findPaymentsBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'SUCCESS' AND p.createdAt BETWEEN :startDate AND :endDate")
    Double getRevenueBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate);
}