package com.ecommerce.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreationResponse {
    private OrderDTO order;
    private QRPaymentResponseDTO qrPayment; // null nếu COD
}
