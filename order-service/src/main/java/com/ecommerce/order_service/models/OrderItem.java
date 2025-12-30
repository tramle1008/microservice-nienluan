package com.ecommerce.order_service.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// OrderItem.java
@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    private Long productId;
    private String productName;
    private String imageUrl;
    private Long variantId;
    private String variantColor;
    private int quantity;
    private BigDecimal unitPrice;      // giá tại thời điểm đặt
    private BigDecimal subtotal;
    private Long discountId;
}