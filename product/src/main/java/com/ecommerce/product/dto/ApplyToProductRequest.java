package com.ecommerce.product.dto;

import lombok.Data;

@Data
public class ApplyToProductRequest {
    private Long productId;
    private Long discountId;
}