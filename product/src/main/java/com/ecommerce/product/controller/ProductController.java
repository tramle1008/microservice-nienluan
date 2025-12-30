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
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
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
    @PutMapping(
            value = "/auth/{productId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long productId,
            @RequestPart("updateDTO") String updateDTOJson,                     // giống add
            @RequestPart(value = "mainImage", required = false) MultipartFile mainImage,
            @RequestPart(name = "variantImages", required = false) List<MultipartFile> variantImages
    ) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        ProductUpdateDTO updateDTO = mapper.readValue(updateDTOJson, ProductUpdateDTO.class);

        ProductDTO updated = productService.updateProductWithVariants(
                productId, updateDTO, mainImage, variantImages != null ? variantImages : Collections.emptyList()
        );

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

    //=============Internal =======

    // 10. Giảm tồn kho (gọi từ Order Service)
    @PutMapping("/internal/variants/{variantId}/reduce-stock")
    public ResponseEntity<ProductVariantDTO> reduceVariantStock(
            @PathVariable Long variantId,
            @RequestParam int quantity
    ) {
        ProductVariantDTO variant = productService.reduceVariantStock(variantId, quantity);
        return ResponseEntity.ok(variant);
    }
    //11. Tăng khi callchanel order tu order-service
    @PutMapping("/internal/variants/{variantId}/increase-stock")
    public ResponseEntity<ProductVariantDTO> increaseVariantStock(
            @PathVariable Long variantId,
            @RequestParam int quantity) {
        ProductVariantDTO variant = productService.increaseVariantStock(variantId, quantity);
        return ResponseEntity.ok(variant);
    }
    //count sp
    @GetMapping("/admin/count")
    public Long countProducts() {
        return productRepository.count();
    }

    //=============NHÓM PUBLIC =======
    // 2. Lấy danh sách phân trang + tìm kiếm + lọc
    @GetMapping("/public")
    public ResponseEntity<ProductResponse> getAllProducts(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize,
            @RequestParam(defaultValue = "productId") String sortBy,
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder
    ) {
        return ResponseEntity.ok(
                productService.getProductsByCategory(categoryId, page, size, sortBy, sortOrder)
        );
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
    //Lay toan bo san pham dang giam gia
    @GetMapping("/public/discounted")
    public ResponseEntity<List<ProductDTO>> getDiscountedProducts() {
        List<ProductDTO> products = productService.getDiscountedProducts();
        return ResponseEntity.ok(products);
    }

    //    Lay theo root (phong ngu, phong tam, ...)
    @GetMapping("/public/tree/{rootId}")
    public ResponseEntity<Page<ProductDTO>> getProductsInTree(
            @PathVariable Long rootId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder
    ) {
        Sort sort = sortOrder.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(productService.getProductsInTree(rootId, pageable));
    }


    //Tìm kiếm bằng hình ảnh
    @PostMapping(value = "/public/search-by-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ProductDTO>> searchByImage(
            @RequestParam("image") MultipartFile image) throws IOException {

        List<ProductDTO> products = productService.searchProductsBySimilarImage(image);
        return ResponseEntity.ok(products);
    }

}