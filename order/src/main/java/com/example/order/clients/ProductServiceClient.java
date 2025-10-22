package com.example.order.clients;

import com.example.order.dto.ProductDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface ProductServiceClient {
    @GetExchange("/api/products/{productId}")
    ProductDTO getProductDetail(@PathVariable Long productId);
}
