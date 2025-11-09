package com.ecommerce.product.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DiscountCreateDTO {
    @NotBlank
    private String name;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal percentage;

    @DecimalMin("0.0")
    private BigDecimal maxAmount;

    @NotNull
    @FutureOrPresent
    private LocalDateTime startDate;

    @NotNull
    @Future
    private LocalDateTime endDate;

    private boolean active = true;

    @NotBlank
    private String target; // PRODUCT, VARIANT, CATEGORY, GLOBAL

    @NotBlank
    private String type;   // PERCENTAGE, FIXED_AMOUNT
}