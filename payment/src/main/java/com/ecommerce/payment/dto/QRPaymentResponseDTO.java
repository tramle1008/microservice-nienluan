package com.ecommerce.payment.dto;

import java.math.BigDecimal;

public record QRPaymentResponseDTO(
        String transactionCode,
        BigDecimal amount,
        String qrUrl
) {}