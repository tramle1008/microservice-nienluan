package com.ecommerce.order_service.dto;

import com.ecommerce.order_service.models.OrderMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminPendingOrderDTO(
        Long orderId,
        String code,
        Long userId,
        String customerName,
        String customerEmail,
        String phoneNumber,
        String shippingAddress,
        String status,

        BigDecimal totalAmount,
        BigDecimal shippingFee,

        LocalDateTime orderDate,
        LocalDateTime paidAt,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt,
        LocalDateTime cancelledAt,

        Long paymentId,
        OrderMethod method
) {}