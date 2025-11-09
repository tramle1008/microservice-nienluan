package com.ecommerce.product.repository;

import com.ecommerce.product.models.Discount;
import com.ecommerce.product.models.Product;
import com.ecommerce.product.models.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DiscountRepository extends JpaRepository<Discount, Long> {

    @Query("SELECT d FROM Discount d " +
            "JOIN ProductDiscount pd ON pd.discount.discountId = d.discountId " +
            "WHERE pd.product.productId = :productId " +
            "AND d.active = true " +
            "AND CURRENT_TIMESTAMP BETWEEN d.startDate AND d.endDate")
    List<Discount> findActiveDiscountsForProduct(@Param("productId") Long productId);

    // Lấy discount đang áp dụng cho variant
    @Query("SELECT d FROM Discount d " +
            "JOIN VariantDiscount vd ON vd.discount.discountId = d.discountId " +
            "WHERE vd.variant.variantId = :variantId " +
            "AND d.active = true " +
            "AND CURRENT_TIMESTAMP BETWEEN d.startDate AND d.endDate")
    List<Discount> findActiveDiscountsForVariant(@Param("variantId") Long variantId);

    List<Discount> findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            java.time.LocalDateTime now1, java.time.LocalDateTime now2);
}
