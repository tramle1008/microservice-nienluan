package com.ecommerce.product.dto;

import lombok.Data;
import java.util.List;

@Data
public class CategoryTreeDTO {
    private Long categoryId;
    private String categoryName;
    private List<CategoryTreeDTO> children;
}