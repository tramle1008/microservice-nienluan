package com.ecommerce.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
@Order(1) // 1. THÊM @Order(1) ĐỂ CHẠY SỚM
public class AuthenticationFilter implements GlobalFilter {

    private final WebClient webClient;

    public AuthenticationFilter(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("lb://AUTH-SERVICE").build();
    }

    // 2. DÙNG Set.of() THAY List.of()
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/auth/",
            "/api/address/provinces",
            "/api/address/wards",
            "/api/products/public",
            "/api/categories/public",
            "/api/discounts/public/",
            "/api/variants/public/",
            "/api/shipping/fee/province/",
            "/api/image/",
            "/eureka/",
            "/api/payments/callback"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString(); // 3. DÙNG getPath() THAY getURI().getPath()

        // 4. THÊM LOG ĐỂ DEBUG (XÓA SAU KHI FIX)
        System.out.println("GATEWAY FILTER - Path: " + path);
        boolean isPublic = PUBLIC_PATHS.stream().anyMatch(path::startsWith);
        System.out.println("IS PUBLIC API? " + isPublic);

        if (isPublic) {
            System.out.println("BỎ QUA XÁC THỰC: " + path);
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("401 - KHÔNG CÓ TOKEN");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        return webClient.get()
                .uri("/api/auth/validate?token=" + token)
                .retrieve()
                .onStatus(status -> status.isError(), response -> Mono.error(new RuntimeException("Invalid token")))
                .bodyToMono(UserInfo.class)
                // Trong AuthenticationFilter.java
                .flatMap(userInfo -> {
                    String userIdStr = userInfo.getId();    // backend auth trả về "2"
                    String userRole = userInfo.getRole();

                    String finalRoles = Set.of("ROLE_USER", "ROLE_ADMIN").contains(userRole)
                            ? userRole + ",ROLE_SERVICE"
                            : userRole;

                    // QUAN TRỌNG: truyền đúng kiểu String số, không thêm khoảng trắng
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .header("X-User-Id", userIdStr)           // "2" → Spring sẽ convert thành Long 2
                            .header("X-User-Role", finalRoles)
                            .build();

                    // Đúng cách mutate exchange
                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(mutatedRequest)
                            .build();

                    return chain.filter(mutatedExchange);
                })
                .onErrorResume(e -> {
                    System.out.println("TOKEN KHÔNG HỢP LỆ: " + e.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }
}