package com.example.order.controller;

import com.example.order.dto.CartItemRequest;
import com.example.order.models.CartItem;
import com.example.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    CartService cartService;

    @PostMapping()
    public ResponseEntity<String> addToCart(
            @RequestHeader("X-User-ID") Long userId,
            @RequestBody CartItemRequest request){

        try {
            cartService.addToCart(userId, request);
            return ResponseEntity.ok("Thêm sản phẩm thành công");

        } catch (RuntimeException ex) {
            switch (ex.getMessage()) {
                case "PRODUCT_NOT_FOUND":
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy sản phẩm");
                case "OUT_OF_STOCK":
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Sản phẩm hết hàng");
                case "USER_NOT_FOUND":
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng");
                default:
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi không xác định");
            }
        }
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<String> removeFromCart(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable Long productId) {

        boolean result = cartService.deleteItemFromCart(userId, productId);

        if (result) {
            return ResponseEntity.noContent().build(); // 204 No Content
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Sản phẩm không có trong giỏ hàng"); // 404 Not Found
        }
    }

    @GetMapping()
    public ResponseEntity<List<CartItem>> getCart(
            @RequestHeader("X-User-ID") Long userId) {

        List<CartItem> cartItems = cartService.getCartByUserId(userId);

        if (cartItems.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content nếu giỏ rỗng
        }

        return ResponseEntity.ok(cartItems); // 200 OK + danh sách sản phẩm
    }
}
