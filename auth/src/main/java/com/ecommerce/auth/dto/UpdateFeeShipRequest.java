package com.ecommerce.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFeeShipRequest {
    private BigDecimal defaultFee;
    private BigDecimal freeShippingThreshold;
}
