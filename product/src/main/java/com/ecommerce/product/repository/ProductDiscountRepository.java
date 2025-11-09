// ProductDiscountRepository.java
package com.ecommerce.product.repository;

import com.ecommerce.product.models.Discount;
import com.ecommerce.product.models.Product;
import com.ecommerce.product.models.ProductDiscount;
import com.ecommerce.product.models.ProductDiscountId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductDiscountRepository extends JpaRepository<ProductDiscount, ProductDiscountId> {

    List<ProductDiscount> findByProductProductId(Long productId);
    List<ProductDiscount> findByProduct_ProductId(Long productId);

    // 2. KIỂM TRA ĐÃ APPLY CHƯA (dùng trong applyDiscountToProduct)
    boolean existsByProductAndDiscount(Product product, Discount discount);

}