package com.ecommerce.product.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_variants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "color"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "product")
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long variantId;

    @NotBlank
    private String color;

    @Min(0)
    private Integer stockQuantity = 0;

    @Column(name = "image_path")  // ← ĐỔI TÊN
    private String imagePath;     // ← lưu: variants/xyz.jpg

    private BigDecimal priceOverride;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // discount riêng cho màu
    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VariantDiscount> variantDiscounts = new ArrayList<>();
}