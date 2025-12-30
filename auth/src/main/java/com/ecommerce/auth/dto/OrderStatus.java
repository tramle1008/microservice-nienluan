package com.ecommerce.auth.dto;

public enum OrderStatus {
    PENDING,    // vừa tạo đơn, chưa chọn thanh toán
    CONFIRMED,          // COD – đã xác nhận, chờ giao
    PAID,               // QR đã nhận tiền
    SHIPPED,     // Đơn hàng đã được giao cho đơn vị vận chuyển
    DELIVERED,   // Đơn hàng đã được giao thành công đến khách hàng
    CANCELLED,
    REFUND; // Đơn hàng đã được hoàn tiền (sau khi hủy hoặc trả hàng)
}