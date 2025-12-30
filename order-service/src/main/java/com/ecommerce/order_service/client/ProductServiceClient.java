package com.ecommerce.order_service.client;

import com.ecommerce.order_service.dto.ProductVariantDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ProductServiceClient {

    @Autowired
    private RestTemplate restTemplate;

    private final String PRODUCT_SERVICE = "http://product-service";

    public ProductVariantDTO getVariant(Long variantId) {
        String url = PRODUCT_SERVICE + "/api/variants/public/" + variantId;
        return restTemplate.getForObject(url, ProductVariantDTO.class);
    }

    public void reduceStock(Long variantId, int quantity) {
        String url = PRODUCT_SERVICE + "/api/products/internal/variants/" + variantId + "/reduce-stock?quantity=" + quantity;
        restTemplate.put(url, null, Void.class);
    }

    public void increaseStock(Long variantId, int quantity) {
        String url = PRODUCT_SERVICE + "/api/products/internal/variants/" + variantId + "/increase-stock?quantity=" + quantity;
        restTemplate.put(url, null, Void.class);
    }
}