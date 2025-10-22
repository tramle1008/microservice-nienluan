package com.example.order.service;

import com.example.order.dto.CartItemRequest;
import com.example.order.models.CartItem;

import java.util.List;

public interface CartService  {

    boolean addToCart(Long userId, CartItemRequest request);

      boolean deleteItemFromCart(Long userId, Long productId);


    List<CartItem> getCartByUserId(Long userId);
}
