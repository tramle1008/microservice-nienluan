package com.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private Long productId;
    private String productName;
    private String shortDescription;
    private String longDescription; // HTML
//    private String image;
private String imageUrl;
    private BigDecimal price;
    private BigDecimal finalPrice;
    private List<AppliedDiscountDTO> appliedDiscounts;
    private List<ProductVariantDTO> variants;
//    private MultipartFile imageFile;

}