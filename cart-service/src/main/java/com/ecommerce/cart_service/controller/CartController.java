package com.ecommerce.cart_service.controller;

import com.ecommerce.cart_service.dto.AddToCartRequest;
import com.ecommerce.cart_service.dto.CartDTO;
import com.ecommerce.cart_service.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    @Autowired
    private final CartService cartService;

    @GetMapping("/my")
    public ResponseEntity<CartDTO> getMyCart(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(cartService.getMyCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDTO> addItem(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AddToCartRequest req) {
        return ResponseEntity.ok(cartService.addItem(userId, req));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> updateItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody Map<String, Integer> body) {
        int quantity = body.get("quantity");
        return ResponseEntity.ok(cartService.updateItem(userId, itemId, quantity));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> removeItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(userId, itemId));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@RequestHeader("X-User-Id") Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}