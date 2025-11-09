package com.ecommerce.product.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariantDiscountId implements Serializable {

    @Column(name = "variant_id")
    private Long variantId;

    @Column(name = "discount_id")
    private Long discountId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VariantDiscountId)) return false;
        VariantDiscountId that = (VariantDiscountId) o;
        return Objects.equals(variantId, that.variantId) &&
                Objects.equals(discountId, that.discountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variantId, discountId);
    }
}