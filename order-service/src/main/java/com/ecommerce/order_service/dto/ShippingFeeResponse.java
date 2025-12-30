package com.ecommerce.order_service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShippingFeeResponse {
    private Long provinceId;
    private BigDecimal innerCityFee;
    private BigDecimal defaultFee;
    private BigDecimal freeShippingThreshold;
}