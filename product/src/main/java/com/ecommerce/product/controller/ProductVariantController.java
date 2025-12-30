package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductVariantDTO;
import com.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductService productService;

    @GetMapping("/public/{id}")
    public ResponseEntity<ProductVariantDTO> getVariant(@PathVariable Long id) {
        System.out.println(">>> PUBLIC VARIANT: " + id);
        ProductVariantDTO variant = productService.getVariantById(id);
        return ResponseEntity.ok(variant);
    }

    // LẤY NHIỀU VARIANT CÙNG LÚC
    @GetMapping("/public/batch")
    public ResponseEntity<List<ProductVariantDTO>> getVariantsBatch(
            @RequestParam("ids") List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<ProductVariantDTO> variants = ids.stream()
                .distinct()
                .map(id -> {
                    try {
                        return productService.getVariantById(id);
                    } catch (Exception e) {
                        // Nếu variant bị xóa → bỏ qua
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        return ResponseEntity.ok(variants);
    }
}