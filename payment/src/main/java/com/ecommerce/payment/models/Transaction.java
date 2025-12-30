package com.ecommerce.payment.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "transactions")
@Getter
@Setter @NoArgsConstructor
public class Transaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String gateway;
    private Timestamp transactionDate;
    private String accountNumber;
    private String subAccount;
    private BigDecimal amountIn;
    private BigDecimal amountOut;
    private BigDecimal accumulated;
    private String code;
    private String transactionContent;
    private String referenceNumber;
    private String body;
}