package com.ecommerce.order_service.dto;

import lombok.Data;

@Data
public class CreateOrderRequest {
    private Long addressId; // từ auth-service
    private String paymentMethod; // "COD", "QR"
}