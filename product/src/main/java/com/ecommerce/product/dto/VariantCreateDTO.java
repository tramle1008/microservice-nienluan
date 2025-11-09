package com.ecommerce.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class VariantCreateDTO {
    @NotBlank
    private String color;
    @Min(0)
    private Integer stockQuantity = 0;
    private BigDecimal priceOverride;
    // Không gửi file ở đây → gửi riêng
}