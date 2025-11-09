package com.ecommerce.cart_service.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// CartItem.java
@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
public class CartItem {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    private Long productId;
    private Long variantId;
    private String productName;
    private String imageUrl;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    private LocalDateTime reservedUntil;
}