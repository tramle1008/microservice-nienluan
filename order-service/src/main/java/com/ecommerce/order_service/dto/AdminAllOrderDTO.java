package com.ecommerce.order_service.dto;

import com.ecommerce.order_service.models.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminAllOrderDTO(
        Long orderId,
        String code,
        String customerName,
        String customerEmail,
        String phoneNumber,
        String shippingAddress,
        OrderStatus status,
        BigDecimal totalAmount,
        BigDecimal shippingFee,
        BigDecimal finalAmount,
        LocalDateTime orderDate,
        LocalDateTime paidAt,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt,
        LocalDateTime cancelledAt
) {}