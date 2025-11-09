package com.ecommerce.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductCreateDTO {
    @NotBlank
    private String productName;

    @Size(max = 500)
    private String shortDescription;

    @NotBlank
    private String longDescription; // HTML

    private BigDecimal price;
    private Long categoryId;

    @NotEmpty
    private List<VariantCreateDTO> variants;
}