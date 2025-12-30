package com.ecommerce.product.service;

import com.ecommerce.product.dto.DiscountCreateDTO;
import com.ecommerce.product.dto.DiscountDTO;
import com.ecommerce.product.dto.DiscountResponse;
import com.ecommerce.product.dto.DiscountUpdateDTO;
import com.ecommerce.product.models.Discount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DiscountService {
    DiscountDTO createDiscount(DiscountCreateDTO dto);
    List<DiscountDTO> getActiveDiscounts();
    DiscountDTO updateDiscount(Long id, DiscountUpdateDTO dto);

    void applyDiscountToProduct(Long productId, Long discountId);

    void applyDiscountToVariant(Long variantId, Long discountId);
    DiscountDTO mapToDTO(Discount discount);

    void deleteDiscount(Long id);

    DiscountResponse getAllDiscounts(
            int pageNumber, int pageSize, String sortBy, String sortOrder,
            String keyword, Boolean active);
}
