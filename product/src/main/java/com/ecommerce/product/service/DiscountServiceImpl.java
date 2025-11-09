package com.ecommerce.product.service;

import com.ecommerce.product.dto.DiscountCreateDTO;
import com.ecommerce.product.dto.DiscountDTO;
import com.ecommerce.product.dto.DiscountUpdateDTO;
import com.ecommerce.product.exceptions.ResourceNotFoundException;
import com.ecommerce.product.exceptions.BadRequestException;
import com.ecommerce.product.models.*;
import com.ecommerce.product.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DiscountServiceImpl implements DiscountService {
    @Autowired private DiscountRepository discountRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductVariantRepository productVariantRepository;
    @Autowired private VariantDiscountRepository variantDiscountRepository;
    @Autowired private ProductDiscountRepository productDiscountRepository;
//    @Autowired private ProductService productService;


    @Override
    public DiscountDTO createDiscount(DiscountCreateDTO dto) {
        Discount discount = new Discount();
        discount.setName(dto.getName());
        discount.setPercentage(dto.getPercentage());
        discount.setMaxAmount(dto.getMaxAmount());
        discount.setStartDate(dto.getStartDate());
        discount.setEndDate(dto.getEndDate());
        discount.setActive(dto.isActive());
        discount.setTarget(DiscountTarget.valueOf(dto.getTarget()));
        discount.setType(DiscountType.valueOf(dto.getType()));

        discount = discountRepository.save(discount);

        return mapToDTO(discount);
    }

    @Override
    public List<DiscountDTO> getActiveDiscounts() {
        LocalDateTime now = LocalDateTime.now();
        return discountRepository.findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(now, now)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DiscountDTO updateDiscount(Long id, DiscountUpdateDTO dto) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount not found"));

        if (dto.getName() != null) discount.setName(dto.getName());
        if (dto.getPercentage() != null) discount.setPercentage(dto.getPercentage());
        if (dto.getMaxAmount() != null) discount.setMaxAmount(dto.getMaxAmount());
        if (dto.getStartDate() != null) discount.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) discount.setEndDate(dto.getEndDate());
        if (dto.getActive() != null) discount.setActive(dto.getActive());
        if (dto.getTarget() != null) discount.setTarget(DiscountTarget.valueOf(dto.getTarget()));
        if (dto.getType() != null) discount.setType(DiscountType.valueOf(dto.getType()));

        discount = discountRepository.save(discount);
        return mapToDTO(discount);
    }

    private DiscountDTO mapToDTO(Discount d) {
        return new DiscountDTO(
                d.getDiscountId(),
                d.getName(),
                d.getPercentage(),
                d.getMaxAmount(),
                d.getStartDate(),
                d.getEndDate(),
                d.isActive(),
                d.getTarget().name(),
                d.getType().name()
        );
    }

    // ==============================
    // APPLY DISCOUNT TO PRODUCT / VARIANT
    // ==============================

    @Override
    @Transactional
    public void applyDiscountToProduct(Long productId, Long discountId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new ResourceNotFoundException("Discount", "id", discountId));

        if (!discount.isActive() || !discount.isWithinDateRange()) {
            throw new BadRequestException("Discount is not active or out of date");
        }

        if (productDiscountRepository.existsByProductAndDiscount(product, discount)) {
            throw new BadRequestException("Discount already applied to product");
        }

        ProductDiscount pd = new ProductDiscount(product, discount);
        productDiscountRepository.save(pd);

        updateProductFinalPrice(product);
    }

    @Override
    @Transactional
    public void applyDiscountToVariant(Long variantId, Long discountId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant", "id", variantId));

        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new ResourceNotFoundException("Discount", "id", discountId));

        if (!discount.isActive() || !discount.isWithinDateRange()) {
            throw new BadRequestException("Discount is not active or out of date");
        }

        if (variantDiscountRepository.existsByVariantAndDiscount(variant, discount)) {
            throw new BadRequestException("Discount already applied to this variant");
        }

        VariantDiscount vd = new VariantDiscount(variant, discount);
        variantDiscountRepository.save(vd);

        updateProductFinalPrice(variant.getProduct());
    }

    // ==============================
    // TÍNH GIÁ CUỐI CÙNG (TỰ ĐỘNG CẬP NHẬT)
    // ==============================

    private BigDecimal calculateDiscount(Discount d, BigDecimal price) {
        if (d.getType() == DiscountType.PERCENTAGE) {
            BigDecimal reduction = price.multiply(d.getPercentage()).divide(BigDecimal.valueOf(100));
            if (d.getMaxAmount() != null) {
                reduction = reduction.min(d.getMaxAmount());
            }
            return reduction;
        } else {
            return d.getMaxAmount() != null ? d.getMaxAmount().min(price) : BigDecimal.ZERO;
        }
    }

    private BigDecimal calculateFinalPrice(Product product, ProductVariant variant) {
        BigDecimal basePrice = variant != null && variant.getPriceOverride() != null
                ? variant.getPriceOverride()
                : product.getPrice();

        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discountAmount = BigDecimal.ZERO;

        // 1. Ưu tiên: Discount của Variant
        if (variant != null) {
            List<Discount> discounts = discountRepository.findActiveDiscountsForVariant(variant.getVariantId());
            for (Discount d : discounts) {
                discountAmount = discountAmount.max(calculateDiscount(d, basePrice));
            }
        }

        // 2. Nếu chưa có → dùng discount của Product
        if (discountAmount.compareTo(BigDecimal.ZERO) == 0) {
            List<Discount> discounts = discountRepository.findActiveDiscountsForProduct(product.getProductId());
            for (Discount d : discounts) {
                discountAmount = discountAmount.max(calculateDiscount(d, basePrice));
            }
        }

        return basePrice.subtract(discountAmount);
    }

    private void updateProductFinalPrice(Product product) {
        BigDecimal finalPrice = calculateFinalPrice(product, null);
        product.setFinalPrice(finalPrice);
        productRepository.save(product);
    }
}
