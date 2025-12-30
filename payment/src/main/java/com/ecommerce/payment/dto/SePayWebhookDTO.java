package com.ecommerce.payment.dto;

public record SePayWebhookDTO(
        String gateway,
        String transactionDate,
        String accountNumber,
        String subAccount,
        String transferType,
        String transferAmount,
        String accumulated,
        String code,
        String content,
        String referenceCode,
        String description
) {}
