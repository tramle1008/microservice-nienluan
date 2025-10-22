package com.example.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO {
    private Long orderItemId;
    private Long productId;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subTotal;


    public OrderItemDTO(Long productId, Integer quantity, BigDecimal price, BigDecimal multiply) {
    }


}
