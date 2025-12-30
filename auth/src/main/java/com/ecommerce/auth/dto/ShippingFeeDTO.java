package com.ecommerce.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingFeeDTO {
    private Long provinceId;
    private String provinceName;
    private BigDecimal fee;
    private String wardName;
    private BigDecimal defaultFee;
    private BigDecimal freeShippingThreshold;
}
