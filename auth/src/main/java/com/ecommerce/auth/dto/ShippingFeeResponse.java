package com.ecommerce.auth.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShippingFeeResponse {
    private Long provinceId;
    private String province;
    private String ward;
    private String detail;
    private String phoneNumber;
    private BigDecimal defaultFee;
    private BigDecimal freeShippingThreshold;
}