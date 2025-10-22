package com.ecommerce.product.service;

import com.ecommerce.product.dto.CategoryDTO;
import com.ecommerce.product.dto.CategoryResponse;
import com.ecommerce.product.exceptions.APIException;
import com.ecommerce.product.exceptions.ResourceNotFoundException;
import com.ecommerce.product.models.Category;
import com.ecommerce.product.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceIml implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper ;

    @Override
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortBy_Order = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortBy_Order);
        Page<Category> categoryPage = categoryRepository.findAll(pageDetails);
        List<Category> categories = categoryPage.getContent();
// làm thêm cái exception nếu category trống thì thông báo là chưa tạo category nào
        List<CategoryDTO> categoryDTOS = categories.stream()
                .map(category -> modelMapper.map(category,CategoryDTO.class))
                .collect(Collectors.toList());

        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDTOS);
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setTotalElements(categoryPage.getTotalElements());
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setLastPage(categoryResponse.isLastPage());
        return  categoryResponse;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {

        // Kiểm tra trùng tên
        Category existingCategory = categoryRepository.findByCategoryName(categoryDTO.getCategoryName());

        if (existingCategory != null) {
            throw new APIException("Category with name '" + categoryDTO.getCategoryName() + "' already exists.");
        }

        Category category = modelMapper.map(categoryDTO,Category.class);


        Category saveCategory = categoryRepository.save(category);
        CategoryDTO saveCategoryDTO = modelMapper.map(saveCategory,CategoryDTO.class);
        return saveCategoryDTO;
    }


    @Override
    public List<CategoryDTO> createbatchCategories(List<CategoryDTO> categoryDTOs) {
        List<CategoryDTO> savedCategories = new ArrayList<>();
        for (CategoryDTO categoryDTO : categoryDTOs) {

            Category category = modelMapper.map(categoryDTO, Category.class);
            Category saved = categoryRepository.save(category);
            CategoryDTO savedDTO = modelMapper.map(saved, CategoryDTO.class);
            savedCategories.add(savedDTO);
        }
        return savedCategories;

    }


    @Override
    public String deleteCategory(Long categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category","categoryId",categoryId));
        categoryRepository.delete(category) ;
        return "Category id " + categoryId + " deleted successful! ";
    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {
        Category up_category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category","categoryId",categoryId));
        Category savecategory = modelMapper.map(categoryDTO,Category.class);
        savecategory.setCategoryId(categoryId);
        Category saveCategory = categoryRepository.save(savecategory);
        CategoryDTO saveCategoryDTO = modelMapper.map(saveCategory,CategoryDTO.class);
        return saveCategoryDTO;
    }
}
