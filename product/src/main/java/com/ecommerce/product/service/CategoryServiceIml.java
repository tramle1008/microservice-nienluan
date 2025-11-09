package com.ecommerce.product.service;

import com.ecommerce.product.dto.*;
import com.ecommerce.product.exceptions.APIException;
import com.ecommerce.product.exceptions.ResourceNotFoundException;
import com.ecommerce.product.models.Category;
import com.ecommerce.product.models.Product;
import com.ecommerce.product.repository.CategoryRepository;
import com.ecommerce.product.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceIml implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ModelMapper modelMapper;

    // === CŨ ===
    @Override
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Category> page = categoryRepository.findChildCategories(pageable);

        List<CategoryDTO> dtos = page.getContent().stream()
                .map(category -> {
                    CategoryDTO dto = new CategoryDTO();
                    dto.setCategoryId(category.getCategoryId());
                    dto.setCategoryName(category.getCategoryName());
                    dto.setParentId(category.getParent() != null ? category.getParent().getCategoryId() : null);
                    return dto;
                })
                .collect(Collectors.toList());

        CategoryResponse response = new CategoryResponse();
        response.setContent(dtos);
        response.setPageNumber(page.getNumber());
        response.setPageSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setLastPage(page.isLast());

        if (dtos.isEmpty()) {
            throw new APIException("Chưa có danh mục nào được tạo.");
        }
        return response;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category existing = categoryRepository.findByCategoryName(categoryDTO.getCategoryName());
        if (existing != null) {
            throw new APIException("Tên danh mục đã tồn tại: " + categoryDTO.getCategoryName());
        }
        Category category = modelMapper.map(categoryDTO, Category.class);
        Category saved = categoryRepository.save(category);
        return modelMapper.map(saved, CategoryDTO.class);
    }



    @Override
    public String deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        categoryRepository.delete(category);
        return "Xóa thành công category ID: " + categoryId;
    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        category.setCategoryName(categoryDTO.getCategoryName());
        Category saved = categoryRepository.save(category);
        return modelMapper.map(saved, CategoryDTO.class);
    }

    // === MỚI ===

    @Override
    public List<CategoryTreeDTO> getCategoryTree() {
        List<Category> roots = categoryRepository.findRootCategories();
        return roots.stream()
                .map(this::buildTree)
                .collect(Collectors.toList());
    }

    private CategoryTreeDTO buildTree(Category category) {
        CategoryTreeDTO dto = new CategoryTreeDTO();
        dto.setCategoryId(category.getCategoryId());
        dto.setCategoryName(category.getCategoryName());

        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            dto.setChildren(category.getChildren().stream()
                    .map(this::buildTree)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    @Override
    public List<ProductDTO> getProductsByCategoryId(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        List<Product> products = productRepository.findByCategoryIdWithVariants(categoryId);
        return products.stream()
                .map(p -> {
                    ProductDTO dto = modelMapper.map(p, ProductDTO.class);
                    dto.setVariants(p.getVariants().stream()
                            .map(v -> modelMapper.map(v, ProductVariantDTO.class))
                            .collect(Collectors.toList()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryDTO createRootCategory(String name) {
        if (categoryRepository.findByCategoryName(name) != null) {
            throw new APIException("Root category đã tồn tại: " + name);
        }
        Category root = new Category();
        root.setCategoryName(name);
        Category saved = categoryRepository.save(root);

        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryId(saved.getCategoryId());
        dto.setCategoryName(saved.getCategoryName());
        dto.setParentId(null);  // ← root không có parent
        return dto;
    }

    @Override
    @Transactional
    public CategoryDTO createCategory(String name, Long parentId) {
        Category parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent Category", "parentId", parentId));

        if (categoryRepository.findByCategoryNameAndParent(name, parent) != null) {
            throw new APIException("Danh mục con đã tồn tại với tên: " + name);
        }

        Category child = new Category();
        child.setCategoryName(name);
        child.setParent(parent);
        Category saved = categoryRepository.save(child);
        return modelMapper.map(saved, CategoryDTO.class);
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(Long categoryId, String newName) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        category.setCategoryName(newName);
        Category saved = categoryRepository.save(category);
        return modelMapper.map(saved, CategoryDTO.class);
    }

    @Override
    @Transactional
    public String deleteRootByName(String rootName) {
        Category root = categoryRepository.findRootByName(rootName)
                .orElseThrow(() -> new ResourceNotFoundException("Root Category", "name", rootName));
        categoryRepository.delete(root);
        return "Xóa thành công root: " + rootName + " và tất cả danh mục con";
    }

    @Override
    public List<Long> getAllChildIds(Long rootId) {
        List<Long> ids = new ArrayList<>();
        Category root = categoryRepository.findById(rootId).orElse(null);
        if (root != null) {
            collectIds(root, ids);
        }
        return ids;
    }

    private void collectIds(Category cat, List<Long> ids) {
        ids.add(cat.getCategoryId());
        if (cat.getChildren() != null) {
            for (Category child : cat.getChildren()) {
                collectIds(child, ids);
            }
        }
    }
}