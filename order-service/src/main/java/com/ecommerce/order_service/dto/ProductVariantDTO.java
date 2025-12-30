package com.ecommerce.order_service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductVariantDTO {
    private Long variantId;
    private String color;
    private Integer stockQuantity;
    private String imageUrl;
    private BigDecimal priceOverride;
    private BigDecimal finalPrice; // ← BẮT BUỘC
    private Long productId;
    private String productName;
    private List<AppliedDiscountDTO> appliedProductDiscounts;
    private List<AppliedDiscountDTO> appliedDiscounts;
}