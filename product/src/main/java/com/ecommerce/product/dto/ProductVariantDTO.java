package com.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantDTO {
    private Long variantId;
    private Long productId;           // THÊM
    private String productName;
    private String color;
    private Integer stockQuantity;
    private String imageUrl;
    private BigDecimal priceOverride;
    private BigDecimal finalPrice;
    private List<AppliedDiscountDTO> appliedDiscounts;
}