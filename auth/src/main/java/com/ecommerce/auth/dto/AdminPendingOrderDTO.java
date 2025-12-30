package com.ecommerce.auth.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminPendingOrderDTO(
        Long orderId,
        String code,
        Long userId,
        String customerName,      // bạn có thể join từ User hoặc lưu trong Order
        String phone,
        String address,
        BigDecimal totalAmount,
        LocalDateTime orderDate
) {}