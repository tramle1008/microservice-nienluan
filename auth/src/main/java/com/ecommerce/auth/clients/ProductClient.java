package com.ecommerce.auth.clients;

import com.ecommerce.auth.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class ProductClient {

    private final RestTemplate restTemplate;
    private static final String PRODUCT_SERVICE_URL = "http://PRODUCT-SERVICE";

    public long getTotalProducts() {
        String url = PRODUCT_SERVICE_URL + "/api/products/admin/count";
        return restTemplate.getForObject(url, Long.class);
    }

    public long getTotalCategories() {
        String url = PRODUCT_SERVICE_URL + "/api/categories/admin/count";
        return restTemplate.getForObject(url, Long.class);
    }
    public ProductResponse getAllProducts(
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortOrder,
            String keyword,
            Long categoryId) {

        String url = PRODUCT_SERVICE_URL + "/api/products/public";

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("pageNumber", pageNumber)
                .queryParam("pageSize", pageSize)
                .queryParam("sortBy", sortBy)
                .queryParam("sortOrder", sortOrder);

        if (keyword != null && !keyword.isBlank()) {
            builder.queryParam("keyword", keyword);
        }
        if (categoryId != null) {
            builder.queryParam("categoryId", categoryId);
        }

        String finalUrl = builder.toUriString();

        // Dùng getForEntity để lấy full ResponseEntity (có status, header...)
        ResponseEntity<ProductResponse> responseEntity =
                restTemplate.getForEntity(finalUrl, ProductResponse.class);

        return responseEntity.getBody();
    }

}