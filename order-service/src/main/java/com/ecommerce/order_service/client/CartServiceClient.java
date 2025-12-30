package com.ecommerce.order_service.client;

import com.ecommerce.order_service.dto.CartDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

// CartServiceClient.java
@Service
public class CartServiceClient {

    @Autowired
    private RestTemplate restTemplate;

    private final String CART_SERVICE = "http://cart-service";

    public CartDTO getCurrentCart(Long userId) {
        String url = CART_SERVICE + "/api/carts/my";
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", String.valueOf(userId));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<CartDTO> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, CartDTO.class);
        return response.getBody();
    }

    public void clearCart(Long userId) {
        String url = CART_SERVICE + "/api/carts/clear";
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", String.valueOf(userId));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }
}