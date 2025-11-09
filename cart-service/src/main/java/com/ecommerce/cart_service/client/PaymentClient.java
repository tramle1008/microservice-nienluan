package com.ecommerce.cart_service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentClient {

    private final WebClient.Builder webClient;

    public Mono<String> createPayment(Long orderId, BigDecimal amount) {
        return webClient.build()
                .post()
                .uri("lb://PAYMENT-SERVICE/api/payments")
                .bodyValue(Map.of("orderId", orderId, "amount", amount))
                .retrieve()
                .bodyToMono(String.class)  // trả về URL thanh toán
                .timeout(Duration.ofSeconds(10));
    }
}