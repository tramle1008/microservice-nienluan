package com.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountDTO {
    private Long discountId;
    private String name;
    private BigDecimal percentage;
    private BigDecimal maxAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;
    private String target; // PRODUCT, VARIANT, CATEGORY, GLOBAL
    private String type;   // PERCENTAGE, FIXED_AMOUNT
}