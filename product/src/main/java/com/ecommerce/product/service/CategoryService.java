package com.ecommerce.product.service;

import com.ecommerce.product.dto.*;

import java.util.List;

public interface CategoryService {
    // Cũ
    CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    CategoryDTO createCategory(CategoryDTO categoryDTO);
//    List<CategoryDTO> createbatchCategories(List<CategoryDTO> categoryDTOs);
    String deleteCategory(Long categoryId);
    CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId);

    // MỚI
    List<CategoryTreeDTO> getCategoryTree();
    List<ProductDTO> getProductsByCategoryId(Long categoryId);

    CategoryDTO createRootCategory(String name);
    CategoryDTO createCategory(String name, Long parentId);

    CategoryDTO updateCategory(Long categoryId, String newName);
    String deleteRootByName(String rootName);

    List<Long> getAllChildIds(Long rootId);
}