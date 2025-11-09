package com.ecommerce.cart_service.client;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderClient {

    private final WebClient.Builder webClient;

    @CircuitBreaker(name = "order-service", fallbackMethod = "fallbackCreateOrder")
    @Retry(name = "order-service")
    @TimeLimiter(name = "order-service")
    public Mono<OrderResponse> createOrder(OrderCreateRequest request) {
        return webClient.build()
                .post()
                .uri("lb://ORDER-SERVICE/api/orders")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OrderResponse.class)
                .timeout(Duration.ofSeconds(10));
    }

    public Mono<OrderResponse> fallbackCreateOrder(OrderCreateRequest req, Throwable t) {
        log.warn("Order service unavailable: {}", t.getMessage());
        return Mono.just(OrderResponse.failed("Order service is down"));
    }
}