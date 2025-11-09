package com.ecommerce.product.dto;

import lombok.Data;

@Data
public class CategoryCreateWithParentDTO {
    private String categoryName;
    private Long parentId;
}