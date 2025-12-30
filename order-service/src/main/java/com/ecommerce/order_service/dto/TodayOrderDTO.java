package com.ecommerce.order_service.dto;

import com.ecommerce.order_service.models.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    // List<OrderItemDTO> orderItemDTOList; //nếu cần chi tiết sản phẩm
}