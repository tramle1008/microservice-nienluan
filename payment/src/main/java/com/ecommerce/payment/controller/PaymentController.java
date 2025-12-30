package com.ecommerce.payment.controller;

import com.ecommerce.payment.dto.CreatePaymentRequest;
import com.ecommerce.payment.dto.QRPaymentResponseDTO;
import com.ecommerce.payment.dto.SePayWebhookDTO;
import com.ecommerce.payment.models.PaymentStatus;
import com.ecommerce.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    record InitPaymentRequest(Long orderId) {}

    @PostMapping("/init")
    public ResponseEntity<QRPaymentResponseDTO> initPayment(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody InitPaymentRequest request) {
        log.info("Received X-User-Id: {}", userId);
        return ResponseEntity.ok(paymentService.initPayment(userId, request.orderId));
    }


    @PostMapping("/callback")
    public ResponseEntity<Map<String, Boolean>> handleWebhook(@RequestBody SePayWebhookDTO dto) {
        paymentService.handleSePayWebhook(dto);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/status/{transactionCode}")
    public ResponseEntity<PaymentStatus> getStatus(@PathVariable String transactionCode) {
        return ResponseEntity.ok(paymentService.getPaymentStatus(transactionCode));
    }

    @GetMapping
    public ResponseEntity<String> test(){
        return ResponseEntity.ok("Test");
    }
}