package com.example.ecoms.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = { "user", "orderItems" })
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime orderDate = LocalDateTime.now();

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING; // default value

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference // avoid infinite recursion
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    // ✅ Payment fields for Razorpay integration
    @Column(name = "payment_id")
    private String paymentId;  // Razorpay order ID

    @Column(name = "payment_status")
    private String paymentStatus;  // PENDING / SUCCESS / FAILED

    @Column(name = "razorpay_key_id")
    private String razorpayKeyId;  // Public key for frontend
}
