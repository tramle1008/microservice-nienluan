package com.ecommerce.product.service;

import com.ecommerce.product.dto.DiscountCreateDTO;
import com.ecommerce.product.dto.DiscountDTO;
import com.ecommerce.product.dto.DiscountUpdateDTO;

import java.util.List;

public interface DiscountService {
    DiscountDTO createDiscount(DiscountCreateDTO dto);
    List<DiscountDTO> getActiveDiscounts();
    DiscountDTO updateDiscount(Long id, DiscountUpdateDTO dto);

    void applyDiscountToProduct(Long productId, Long discountId);

    void applyDiscountToVariant(Long variantId, Long discountId);
}
