package com.ecommerce.order_service.dto;


import com.ecommerce.order_service.models.OrderItem;
import lombok.*;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Long productId;
    private String productName;
    private String imageUrl;
    private Long variantId;
    private String variantColor;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private Long discountId;

    // CHUYỂN OrderItem → DTO
    public static OrderItemDTO from(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductName());
        dto.setImageUrl(item.getImageUrl());
        dto.setVariantId(item.getVariantId());
        dto.setVariantColor(item.getVariantColor());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setSubtotal(item.getSubtotal());
        dto.setDiscountId(item.getDiscountId());
        return dto;
    }
}