package com.ecommerce.product.service;

import com.ecommerce.product.dto.*;
import com.ecommerce.product.models.Product;
import com.ecommerce.product.models.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    // ===================================================================
    // 1. TẠO SẢN PHẨM + VARIANT + ẢNH (1 LẦN DUY NHẤT)
    // ===================================================================
    ProductDTO addProductWithVariants(
            Long categoryId,
            ProductCreateDTO createDTO,
            MultipartFile mainImage,
            List<MultipartFile> variantImages
    ) throws IOException;

    // ===================================================================
    // 2. LẤY DANH SÁCH (PHÂN TRANG + TÌM KIẾM + LỌC)
    // ===================================================================
    ProductResponse getAllProducts(
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortOrder,
            String keyword,
            Long categoryId
    );

    // ===================================================================
    // 3. LẤY THEO ID (CÓ VARIANT + FINAL PRICE)
    // ===================================================================
    ProductDTO getProductById(Long productId);

    // ===================================================================
    // 4. LẤY THEO CATEGORY (PHÂN TRANG)
    // ===================================================================
    ProductResponse getProductsByCategory(
            Long categoryId,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortOrder
    );

    // ===================================================================
    // 5. LẤY THEO TỪ KHÓA (TÊN HOẶC MÔ TẢ)
    // ===================================================================
    ProductResponse getProductsByKeyword(
            String keyword,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortOrder
    );

    // ===================================================================
    // 6. CẬP NHẬT SẢN PHẨM (TOÀN BỘ THÔNG TIN + VARIANT)
    // ===================================================================
    ProductDTO updateProduct(Long productId, ProductUpdateDTO updateDTO);

    // ===================================================================
    // 7. XÓA SẢN PHẨM (CASCADE XÓA VARIANT)
    // ===================================================================
    void deleteProduct(Long productId);

    // ===================================================================
    // 8. CẬP NHẬT ẢNH CHÍNH
    // ===================================================================
    ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException;

    // ===================================================================
    // 9. CẬP NHẬT ẢNH CỦA VARIANT (TÙY CHỌN)
    // ===================================================================
    ProductVariantDTO updateVariantImage(Long variantId, MultipartFile image) throws IOException;

    // ===================================================================
    // 10. CẬP NHẬT TỒN KHO VARIANT (KHI ĐẶT HÀNG)
    // ===================================================================
    ProductVariantDTO reduceVariantStock(Long variantId, int quantity);

    BigDecimal calculateFinalPrice(Product product, ProductVariant variant);

    void updateProductFinalPrice(Long productId);

    List<ProductDTO> getDiscountedProducts();


    Page<ProductDTO> getRandomProductsInTree(Long rootId, Pageable pageable);

    ProductVariantDTO getVariantById(Long id);
}