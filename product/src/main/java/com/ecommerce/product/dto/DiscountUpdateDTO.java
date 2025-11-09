package com.ecommerce.product.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DiscountUpdateDTO {
    private String name;
    private BigDecimal percentage;
    private BigDecimal maxAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean active;
    private String target;
    private String type;
}