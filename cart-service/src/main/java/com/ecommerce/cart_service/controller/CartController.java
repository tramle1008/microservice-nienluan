package com.ecommerce.cart_service.controller;

import com.ecommerce.cart_service.dto.AddToCartRequest;
import com.ecommerce.cart_service.dto.CartDTO;
import com.ecommerce.cart_service.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // Helper method để parse an toàn
    private Long parseUserId(String userIdStr) {
        if (userIdStr == null || userIdStr.isBlank()) {
            throw new IllegalArgumentException("X-User-Id is required");
        }
        try {
            return Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid X-User-Id format");
        }
    }

//    Lấy giỏ hàng hiện tại của người dùng
@GetMapping("/my")
public ResponseEntity<CartDTO> getMyCart(@RequestHeader HttpHeaders headers) {
    System.out.println("=== TẤT CẢ HEADER ĐẾN CART-SERVICE ===");
    headers.forEach((k, v) -> System.out.println(k + " = " + v));
    Long userId = Long.valueOf(headers.getFirst("X-User-Id"));
    return ResponseEntity.ok(cartService.getMyCart(userId));
}

//    Thêm sản phẩm vào giỏ hàng
    @PostMapping("/items")
    public ResponseEntity<CartDTO> addItem(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AddToCartRequest req) {
        return ResponseEntity.ok(cartService.addItem(userId, req));
    }

    @PostMapping("/items/{itemId}/increment")
    public ResponseEntity<CartDTO> incrementItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.incrementItem(userId, itemId));
    }

    @PostMapping("/items/{itemId}/decrement")
    public ResponseEntity<CartDTO> decrementItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.decrementItem(userId, itemId));
    }

//    Cập nhật số lượng sản phẩm trong giỏ
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> updateItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody Map<String, Integer> body) {
        int quantity = body.get("quantity");
        return ResponseEntity.ok(cartService.updateItem(userId, itemId, quantity));
    }

//    Xóa sản phẩm khỏi giỏ hàng
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> removeItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(userId, itemId));
    }

//    xoa toan bo gio hang: khi order
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@RequestHeader("X-User-Id") Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}