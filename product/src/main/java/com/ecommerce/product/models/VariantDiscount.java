package com.ecommerce.product.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "variant_discounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariantDiscount {

    @EmbeddedId
    private VariantDiscountId id;

    @MapsId("variantId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @MapsId("discountId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id")
    private Discount discount;

    public VariantDiscount(ProductVariant variant, Discount discount) {
        this.id = new VariantDiscountId(variant.getVariantId(), discount.getDiscountId());
        this.variant = variant;
        this.discount = discount;
    }
}