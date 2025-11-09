package com.ecommerce.product.controller;

import com.ecommerce.product.dto.*;
import com.ecommerce.product.models.Product;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.service.CategoryService;
import com.ecommerce.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ProductRepository productRepository;

//  ==============NHÓM QUẢN LÝ ==============
    // 1. Tạo sản phẩm + variant + ảnh
    @PostMapping(
            value = "/auth/with-variants",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ProductDTO> addProductWithVariants(
            @RequestParam Long categoryId,
            @RequestPart("createDTO") String createDTOJson,  // ← String JSON
            @RequestPart MultipartFile mainImage,
            @RequestPart(name = "variantImages", required = false) List<MultipartFile> variantImages
    ) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        ProductCreateDTO createDTO = mapper.readValue(createDTOJson, ProductCreateDTO.class);

        ProductDTO product = productService.addProductWithVariants(categoryId, createDTO, mainImage, variantImages);
        return ResponseEntity.ok(product);
    }

    // 6. Cập nhật toàn bộ sản phẩm
    @PutMapping("/auth/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long productId,
            @RequestBody ProductUpdateDTO updateDTO
    ) {
        ProductDTO updated = productService.updateProduct(productId, updateDTO);
        return ResponseEntity.ok(updated);
    }

    // 7. Xóa sản phẩm
    @DeleteMapping("/auth/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
    // 8. Cập nhật ảnh chính
    @PatchMapping(value = "/auth/{productId}/main-image", consumes = {"multipart/form-data"})
    public ResponseEntity<ProductDTO> updateProductImage(
            @PathVariable Long productId,
            @RequestPart MultipartFile image
    ) throws IOException {
        ProductDTO updated = productService.updateProductImage(productId, image);
        return ResponseEntity.ok(updated);
    }
    // 9. Cập nhật ảnh variant
    @PatchMapping(value = "/auth/variants/{variantId}/image", consumes = {"multipart/form-data"})
    public ResponseEntity<ProductVariantDTO> updateVariantImage(
            @PathVariable Long variantId,
            @RequestPart MultipartFile image
    ) throws IOException {
        ProductVariantDTO updated = productService.updateVariantImage(variantId, image);
        return ResponseEntity.ok(updated);
    }
    // 10. Giảm tồn kho (gọi từ Order Service)
    @PatchMapping("/auth/variants/{variantId}/reduce-stock")
    public ResponseEntity<ProductVariantDTO> reduceVariantStock(
            @PathVariable Long variantId,
            @RequestParam int quantity
    ) {
        ProductVariantDTO variant = productService.reduceVariantStock(variantId, quantity);
        return ResponseEntity.ok(variant);
    }
//=============NHÓM PUBLIC =======

    // 2. Lấy danh sách phân trang + tìm kiếm + lọc
    @GetMapping("/public")
    public ResponseEntity<ProductResponse> getAllProducts(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId
    ) {
        ProductResponse response = productService.getAllProducts(pageNumber, pageSize, sortBy, sortOrder, keyword, categoryId);
        return ResponseEntity.ok(response);
    }
    // 3. Lấy sản phẩm theo ID
    @GetMapping("/public/{productId}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long productId) {
        ProductDTO product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }
    // 4. Lấy theo category
    @GetMapping("/public/category/{categoryId}")
    public ResponseEntity<ProductResponse> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder
    ) {
        ProductResponse response = productService.getProductsByCategory(categoryId, pageNumber, pageSize, sortBy, sortOrder);
        return ResponseEntity.ok(response);
    }
    // 5. Tìm kiếm theo từ khóa
    @GetMapping("/public/search")
    public ResponseEntity<ProductResponse> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder
    ) {
        ProductResponse response = productService.getProductsByKeyword(keyword, pageNumber, pageSize, sortBy, sortOrder);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/discounted")
    public ResponseEntity<List<ProductDTO>> getDiscountedProducts() {
        List<ProductDTO> products = productService.getDiscountedProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/public/tree/{rootId}")
    public ResponseEntity<Page<ProductDTO>> getRandom(
            @PathVariable Long rootId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> result = productService.getRandomProductsInTree(rootId, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/variants/public/{id}")
    public ResponseEntity<ProductVariantDTO> getVariant(@PathVariable Long id) {
        ProductVariantDTO variant = productService.getVariantById(id);
        return ResponseEntity.ok(variant);
    }
}