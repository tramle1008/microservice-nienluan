package com.ecommerce.cart_service.client;

import com.ecommerce.cart_service.dto.ProductDTO;
import com.ecommerce.cart_service.dto.ProductVariantDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ProductClient {

    private final RestTemplate restTemplate;

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public ProductDTO getProduct(Long productId) {
        return restTemplate.getForObject(
                "http://product-service/api/products/public/{id}",
                ProductDTO.class, productId);
    }

    public ProductVariantDTO getVariant(Long variantId) {
        return restTemplate.getForObject(
                "http://product-service/api/variants/public/{id}",
                ProductVariantDTO.class, variantId);
    }
}