package com.ecommerce.product.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductUpdateDTO {
    private String productName;
    private String shortDescription;
    private String longDescription;
    private BigDecimal price;
    private List<VariantUpdateDTO> variants;
}