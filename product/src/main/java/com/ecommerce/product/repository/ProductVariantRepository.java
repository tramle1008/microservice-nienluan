package com.ecommerce.product.repository;

import com.ecommerce.product.models.Discount;
import com.ecommerce.product.models.Product;
import com.ecommerce.product.models.ProductDiscount;
import com.ecommerce.product.models.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    ProductVariant findByProduct_ProductIdAndColor(Long productId, String color);

    @Modifying
    @Query("UPDATE ProductVariant v SET v.stockQuantity = v.stockQuantity - :qty " +
            "WHERE v.variantId = :variantId AND v.stockQuantity >= :qty")
    int reduceStock(@Param("variantId") Long variantId, @Param("qty") int qty);

    void deleteByProductProductId(Long productId);

    @Query("SELECT v FROM ProductVariant v JOIN FETCH v.product WHERE v.variantId = :id")
    Optional<ProductVariant> findByIdWithProduct(@Param("id") Long id);
}