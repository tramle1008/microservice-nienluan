package com.ecommerce.product.dto;

import lombok.Data;

@Data
public class ApplyToVariantRequest {
    private Long variantId;
    private Long discountId;
}