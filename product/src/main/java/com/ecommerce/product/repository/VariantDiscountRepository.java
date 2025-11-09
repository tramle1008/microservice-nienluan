package com.ecommerce.product.repository;

import com.ecommerce.product.models.Discount;
import com.ecommerce.product.models.ProductVariant;
import com.ecommerce.product.models.VariantDiscount;
import com.ecommerce.product.models.VariantDiscountId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VariantDiscountRepository extends JpaRepository<VariantDiscount, VariantDiscountId> {

    // 2. Kiểm tra đã áp dụng chưa (DÙNG TRONG applyDiscountToVariant)
    boolean existsByVariantAndDiscount(ProductVariant variant, Discount discount);

    // 3. (TÙY CHỌN) Lấy theo discountId
    List<VariantDiscount> findByDiscountDiscountId(Long discountId);

    List<VariantDiscount> findByVariantVariantId(Long variantId);


}