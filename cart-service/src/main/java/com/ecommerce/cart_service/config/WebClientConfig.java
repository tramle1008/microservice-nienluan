package com.ecommerce.cart_service.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebClientConfig {

    @Bean
    @LoadBalanced  // BẮT BUỘC để dùng lb://
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .filter(logRequest())
                .filter(logResponse())
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024));
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(req -> {
            log.info("WebClient → {} {}", req.method(), req.url());
            return Mono.just(req);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(resp -> {
            log.info("WebClient ← {} {}", resp.statusCode(), resp.request().getURI());
            return Mono.just(resp);
        });
    }
}