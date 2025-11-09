package com.ecommerce.product.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "product_discounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDiscount {

    @EmbeddedId
    private ProductDiscountId id;

    @MapsId("productId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @MapsId("discountId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id")
    private Discount discount;

    public ProductDiscount(Product product, Discount discount) {
        this.id = new ProductDiscountId(product.getProductId(), discount.getDiscountId());
        this.product = product;
        this.discount = discount;
    }
}