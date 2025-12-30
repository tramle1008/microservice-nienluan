package com.ecommerce.order_service.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderMethod method;

    private BigDecimal totalAmount;
//    private BigDecimal discountAmount = BigDecimal.ZERO; // tam thoi bo qua se xu ly sao
    private BigDecimal shippingFee = BigDecimal.ZERO;

    private String shippingAddress; // JSON or denormalized
    private String phoneNumber;
    private String customerName;
    private String customerEmail;

    //finalAmount = totalAmount - discountAmount + shippingFee
    public BigDecimal getFinalAmount() {
        return totalAmount
//                .subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO)
                .add(shippingFee != null ? shippingFee : BigDecimal.ZERO);
    }
    private LocalDateTime orderDate = LocalDateTime.now();
    private LocalDateTime paidAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    @Column(unique = true)
    private String code;
    private Long paymentId;

}