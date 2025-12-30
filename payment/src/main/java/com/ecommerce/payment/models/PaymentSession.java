package com.ecommerce.payment.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "payment_session")
public class PaymentSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="transaction_code", unique=true, nullable=false)
    private String transactionCode;
    private Long userId;
    @Column(nullable=false)
    private BigDecimal amount;
    private String currency;
    private String status;
    private String pgName;
    private String pgPaymentId;
    private Long orderId;
    private Timestamp expiresAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}