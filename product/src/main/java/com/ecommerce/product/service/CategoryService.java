package com.ecommerce.product.service;


import com.ecommerce.product.dto.CategoryDTO;
import com.ecommerce.product.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    CategoryDTO createCategory(CategoryDTO categoryDTO);
    List<CategoryDTO> createbatchCategories(List<CategoryDTO> categoryDTOs);
    String deleteCategory(Long categoryid);
    CategoryDTO updateCategory(CategoryDTO category, Long categoryId);
}
