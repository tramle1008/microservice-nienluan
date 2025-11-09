package com.ecommerce.cart_service.service;

import com.ecommerce.cart_service.dto.AddToCartRequest;
import com.ecommerce.cart_service.dto.CartDTO;
import jakarta.validation.Valid;

public interface CartService {
    CartDTO getMyCart(Long userId);

    CartDTO addItem(Long userId, @Valid AddToCartRequest req);

    CartDTO updateItem(Long userId, Long itemId, int quantity);

    CartDTO removeItem(Long userId, Long itemId);

    void clearCart(Long userId);
}
