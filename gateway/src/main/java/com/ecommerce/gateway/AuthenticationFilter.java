package com.ecommerce.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter implements GlobalFilter {

    private final WebClient webClient;

    public AuthenticationFilter(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("lb://AUTH-SERVICE").build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        //  Bỏ qua xác thực cho các API public
        if (path.startsWith("/api/products/public")
                || path.startsWith("/api/auth")
                || path.startsWith("/api/image")
                || path.startsWith("/api/categories/public")
                || path.startsWith("/api/discounts/public")) {
            return chain.filter(exchange);
        }



        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        return webClient.get()
                .uri("/api/auth/validate?token=" + token)
                .retrieve()
                .onStatus(status -> status.isError(), response -> Mono.error(new RuntimeException("Invalid token")))
                .bodyToMono(UserInfo.class)
                .flatMap(userInfo -> {
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .header("X-User-Id", userInfo.getId())
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                });
    }
}

