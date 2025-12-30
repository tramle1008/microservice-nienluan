package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.QRPaymentResponseDTO;
import com.ecommerce.payment.dto.SePayWebhookDTO;
import com.ecommerce.payment.models.PaymentStatus;

public interface PaymentService {
    QRPaymentResponseDTO initPayment(Long userId, Long orderId);
   // QRPaymentResponseDTO createQRPayment(Long userId);          //
    void handleSePayWebhook(SePayWebhookDTO dto);
    PaymentStatus getPaymentStatus(String transactionCode);
}
