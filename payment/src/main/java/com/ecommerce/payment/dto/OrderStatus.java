package com.ecommerce.payment.dto;

public enum OrderStatus {
    PENDING_PAYMENT,    // vừa tạo đơn, chưa chọn thanh toán
    AWAITING_PAYMENT,   // đã chọn QR, đang chờ tiền
    CONFIRMED,          // COD – đã xác nhận, chờ giao
    PAID,               // QR đã nhận tiền
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUND
}