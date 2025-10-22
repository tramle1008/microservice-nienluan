package com.example.order.service;

import com.example.order.clients.ProductServiceClient;
import com.example.order.clients.UserServiceClient;
import com.example.order.dto.CartItemRequest;
import com.example.order.dto.ProductDTO;
import com.example.order.dto.UserDTO;
import com.example.order.models.CartItem;
import com.example.order.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceIml implements CartService {
    private final CartItemRepository cartItemRepository;
    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;

    @Override
    public boolean addToCart(Long userId, CartItemRequest request) {
    //kiem tra
        ProductDTO productResponse = productServiceClient.getProductDetail(request.getProductId());
        if (productResponse.getProductId() == null) {
            throw new RuntimeException("PRODUCT_NOT_FOUND");
        }
        if (productResponse.getQuantity() == null || productResponse.getQuantity() < request.getQuantity()) {
            throw new RuntimeException("OUT_OF_STOCK");
        }

        UserDTO userDTO = userServiceClient.getUserDetail(userId);
        if (userDTO == null || userDTO.getUserId() == null) {
            throw new RuntimeException("USER_NOT_FOUND");
        }
        CartItem existingCartItem = cartItemRepository.findByUserIdAndProductId(userId, request.getProductId());

        if(existingCartItem != null){
            existingCartItem.setQuantity(existingCartItem.getQuantity() + request.getQuantity());
            cartItemRepository.save(existingCartItem);
        }
        else {
            CartItem cartItem = new CartItem();
            cartItem.setQuantity(request.getQuantity());
            cartItem.setUserId(userId);
            cartItem.setProductId(request.getProductId());
//            cartItem.setPrice(BigDecimal.valueOf(100));
            cartItem.setPrice(productResponse.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));

            cartItemRepository.save(cartItem);
        }
        return true;
    }

    @Override
    public boolean deleteItemFromCart(Long userId, Long productId) {
        ProductDTO productResponse = productServiceClient.getProductDetail(productId);
        if (productResponse.getProductId() == null) {
            throw new RuntimeException("PRODUCT_NOT_FOUND");
        }
        if (productResponse.getQuantity() == null || productResponse.getQuantity() < userId) {
            throw new RuntimeException("OUT_OF_STOCK");
        }

        UserDTO userDTO = userServiceClient.getUserDetail(userId);
        if (userDTO == null || userDTO.getUserId() == null) {
            throw new RuntimeException("USER_NOT_FOUND");
        }
        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, productId);
        if (cartItem == null) {
           return false;
        }
        cartItemRepository.delete(cartItem);
        return true;

    }

    @Override
    public List<CartItem> getCartByUserId(Long userId) {
        return cartItemRepository.findAllByUserId(userId);
    }

}
