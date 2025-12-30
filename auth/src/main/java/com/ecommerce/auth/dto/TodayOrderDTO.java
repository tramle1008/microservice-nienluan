package com.ecommerce.auth.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TodayOrderDTO {
    private Long orderId;
    private String code;
    private String customerName;
    private String customerEmail;
    private String phoneNumber;
    private BigDecimal totalAmount;
    private BigDecimal shippingFee;
    private OrderStatus status;
    private LocalDateTime orderDate;
    private LocalDateTime paidAt;
    // Có thể thêm List<OrderItemDTO> nếu cần chi tiết sản phẩm
}