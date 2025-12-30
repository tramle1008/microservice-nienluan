package com.ecommerce.cart_service.client;

import com.ecommerce.cart_service.dto.ProductDTO;
import com.ecommerce.cart_service.dto.ProductVariantDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductClient {

    private final RestTemplate restTemplate;
    private static final String BASE_URL = "http://product-service";

    // LẤY 1 VARIANT
    public ProductVariantDTO getVariant(Long variantId) {
        return restTemplate.getForObject(
                BASE_URL + "/api/variants/public/{id}",
                ProductVariantDTO.class, variantId);
    }

    // MỚI: LẤY NHIỀU VARIANT TRONG 1 LẦN
    public Map<Long, ProductVariantDTO> getVariantsBatch(List<Long> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) {
            return Map.of();
        }

        String ids = variantIds.stream()
                .map(String::valueOf)
                .distinct()
                .collect(Collectors.joining(","));

        ProductVariantDTO[] array = restTemplate.getForObject(
                BASE_URL + "/api/variants/public/batch?ids=" + ids,
                ProductVariantDTO[].class);

        if (array == null) {
            return Map.of();
        }

        return Arrays.stream(array)
                .collect(Collectors.toMap(
                        ProductVariantDTO::getVariantId,
                        Function.identity(),
                        (existing, replacement) -> existing // tránh trùng
                ));
    }
}