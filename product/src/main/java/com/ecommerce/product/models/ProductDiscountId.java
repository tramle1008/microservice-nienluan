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
public class ProductDiscountId implements Serializable {

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "discount_id")
    private Long discountId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductDiscountId)) return false;
        ProductDiscountId that = (ProductDiscountId) o;
        return Objects.equals(productId, that.productId) &&
                Objects.equals(discountId, that.discountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, discountId);
    }
}