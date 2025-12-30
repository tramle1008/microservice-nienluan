package com.ecommerce.order_service.dto;

import java.math.BigDecimal;

public record QRPaymentResponseDTO(
        String transactionCode,
        BigDecimal amount,
        String qrUrl
) {}