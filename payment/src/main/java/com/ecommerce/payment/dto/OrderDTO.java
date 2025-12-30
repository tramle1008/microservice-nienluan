// payment-service/src/main/java/com/ecommerce/payment/dto/OrderDTO.java
// ← CÁI NÀY CHỈ DÙNG ĐỂ NHẬN TỪ ORDER-SERVICE → DÙNG CLASS TRUYỀN THỐNG
package com.ecommerce.payment.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long orderId;
    private String code;
    private OrderStatus status;
    private BigDecimal finalAmount;
    private LocalDateTime orderDate;
    private List<OrderItemDTO> items;
}