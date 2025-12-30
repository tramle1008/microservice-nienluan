package com.ecommerce.product.controller;

import com.ecommerce.product.dto.*;
import com.ecommerce.product.exceptions.BadRequestException;
import com.ecommerce.product.models.Discount;
import com.ecommerce.product.repository.DiscountRepository;
import com.ecommerce.product.service.DiscountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discounts")
public class DiscountController {

    @Autowired private DiscountService discountService;
    @Autowired private DiscountRepository discountRepository;

//    ######## PUBLIC #########################################
//    1. lấy các discounts hoạt động
    @GetMapping("/public/active")
    public ResponseEntity<List<DiscountDTO>> getActive() {
        return ResponseEntity.ok(discountService.getActiveDiscounts());
    }



    @GetMapping("/public")
    public ResponseEntity<DiscountResponse> getAllDiscountsPublic(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "discountId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active) {

        DiscountResponse response = discountService.getAllDiscounts(
                pageNumber, pageSize, sortBy, sortOrder, keyword, active);

        return ResponseEntity.ok(response);
    }

//    ######## AUTH  ADMIN ####################################
//    2. tạo ra discount
    @PostMapping("/create")
    public ResponseEntity<DiscountDTO> create(@Valid @RequestBody DiscountCreateDTO dto) {
        DiscountDTO created = discountService.createDiscount(dto);
        return ResponseEntity.ok(created);
    }

//   3. Thay đổi discount  (name;percentage;maxAmount;startDate;endDate;active;target;type)
    @PutMapping("/create/{id}")
    public ResponseEntity<DiscountDTO> update(@PathVariable Long id, @Valid @RequestBody DiscountUpdateDTO dto) {
        DiscountDTO updated = discountService.updateDiscount(id, dto);
        return ResponseEntity.ok(updated);
    }
//    4. apply cho Product
    @PostMapping("/create/apply/product")
    public ResponseEntity<Void> applyToProduct(@Valid @RequestBody ApplyToProductRequest request) {
        discountService.applyDiscountToProduct(request.getProductId(), request.getDiscountId());
        return ResponseEntity.ok().build();
    }
// 5. apply cho 1 biến thể
    @PostMapping("/create/apply/variant")
    public ResponseEntity<Void> applyToVariant(@Valid @RequestBody ApplyToVariantRequest request) {
        discountService.applyDiscountToVariant(request.getVariantId(), request.getDiscountId());
        return ResponseEntity.ok().build();
    }

//    6. lấy iscount theo id
@GetMapping("/{id}")
public ResponseEntity<DiscountDTO> getDiscountById(@PathVariable Long id) {
    Discount discount = discountRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi ID: " + id));

    return ResponseEntity.ok(discountService.mapToDTO(discount)); // CHẠY NGON!
}

//7. xóa discount
@DeleteMapping("/{id}")
public ResponseEntity<?> delete(@PathVariable Long id) {
    discountService.deleteDiscount(id);
    return ResponseEntity.ok(Map.of("message", "Xóa khuyến mãi thành công!"));
}
}