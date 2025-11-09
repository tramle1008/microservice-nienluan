package com.ecommerce.product.controller;

import com.ecommerce.product.dto.*;
import com.ecommerce.product.service.DiscountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discounts")
public class DiscountController {

    @Autowired private DiscountService discountService;

    @PostMapping("/auth")
    public ResponseEntity<DiscountDTO> create(@Valid @RequestBody DiscountCreateDTO dto) {
        DiscountDTO created = discountService.createDiscount(dto);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/public/active")
    public ResponseEntity<List<DiscountDTO>> getActive() {
        return ResponseEntity.ok(discountService.getActiveDiscounts());
    }

    @PutMapping("/auth/{id}")
    public ResponseEntity<DiscountDTO> update(@PathVariable Long id, @Valid @RequestBody DiscountUpdateDTO dto) {
        DiscountDTO updated = discountService.updateDiscount(id, dto);
        return ResponseEntity.ok(updated);
    }
    @PostMapping("/auth/apply/product")
    public ResponseEntity<Void> applyToProduct(@Valid @RequestBody ApplyToProductRequest request) {
        discountService.applyDiscountToProduct(request.getProductId(), request.getDiscountId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/auth/apply/variant")
    public ResponseEntity<Void> applyToVariant(@Valid @RequestBody ApplyToVariantRequest request) {
        discountService.applyDiscountToVariant(request.getVariantId(), request.getDiscountId());
        return ResponseEntity.ok().build();
    }

}