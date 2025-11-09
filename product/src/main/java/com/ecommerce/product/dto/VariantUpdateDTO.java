package com.ecommerce.product.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VariantUpdateDTO {
    private Long variantId;
    private String color;
    private Integer stockQuantity;
    private BigDecimal priceOverride;
    private String imageUrl;
}