package com.ecommerce.product.controller;

import com.ecommerce.product.config.AppConstants;
import com.ecommerce.product.dto.*;
import com.ecommerce.product.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // 1. Lấy cây danh mục
    @GetMapping("/public/tree")
    public ResponseEntity<List<CategoryTreeDTO>> getCategoryTree() {
        List<CategoryTreeDTO> tree = categoryService.getCategoryTree();
        return ResponseEntity.ok(tree);
    }

    // 2. Lấy sản phẩm theo category ID
    @GetMapping("/public/{categoryId}/products")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable Long categoryId) {
        List<ProductDTO> products = categoryService.getProductsByCategoryId(categoryId);
        return ResponseEntity.ok(products);
    }

    // 3. Tạo root category
    @PostMapping("/auth/create-root")
    public ResponseEntity<CategoryDTO> createRootCategory(@Valid @RequestBody CategoryCreateDTO dto) {
        CategoryDTO created = categoryService.createRootCategory(dto.getCategoryName());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // 4. Tạo category con
    @PostMapping("/auth/create")
    public ResponseEntity<CategoryDTO> createCategory(
            @Valid @RequestBody CategoryCreateWithParentDTO dto) {
        CategoryDTO created = categoryService.createCategory(dto.getCategoryName(), dto.getParentId());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // 5. Cập nhật category bằng ID
    @PutMapping("/auth/update/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryUpdateDTO dto) {
        CategoryDTO updated = categoryService.updateCategory(categoryId, dto.getCategoryName());
        return ResponseEntity.ok(updated);
    }

    // 6. Xóa category bằng ID
    @DeleteMapping("/auth/delete/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long categoryId) {
        String result = categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok(result);
    }

    // 7. Xóa root bằng tên
    @DeleteMapping("/auht/delete/rootName")
    public ResponseEntity<String> deleteRootByName(@RequestParam String rootName) {
        String result = categoryService.deleteRootByName(rootName);
        return ResponseEntity.ok(result);
    }

    // === CÁC API CŨ ===
    @GetMapping("/public")
    public ResponseEntity<CategoryResponse> getCategoryList(
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE_ALL) Integer pageSize,
            @RequestParam(defaultValue = AppConstants.SORT_BY_CATEGORYID) String sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_ORDER_TANG) String sortOrder
    ) {
        CategoryResponse response = categoryService.getAllCategories(pageNumber, pageSize, sortBy, sortOrder);
        return ResponseEntity.ok(response);
    }

}