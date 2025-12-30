package com.ecommerce.order_service.client;

// order-service/src/main/java/com/ecommerce/order_service/client/PaymentClient.java

import com.ecommerce.order_service.dto.InitPaymentRequest;
import com.ecommerce.order_service.dto.QRPaymentResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
@Component
@RequiredArgsConstructor
public class PaymentClient {

    private final RestTemplate restTemplate;

    private static final String PAYMENT_SERVICE_URL = "lb://payment";  // ĐÚNG

    public QRPaymentResponseDTO initPayment(Long orderId, Long userId) {
        String url = PAYMENT_SERVICE_URL + "/api/payments/init";

        Map<String, Long> body = Map.of("orderId", orderId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Id", String.valueOf(userId)); // TRUYỀN THỦ CÔNG – ỔN ĐỊNH NHẤT

        HttpEntity<Map<String, Long>> request = new HttpEntity<>(body, headers);

        return restTemplate.postForEntity(url, request, QRPaymentResponseDTO.class).getBody();
    }
}