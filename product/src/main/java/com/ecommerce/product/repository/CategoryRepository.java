package com.ecommerce.product.repository;

//Crud

import com.ecommerce.product.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Category findByCategoryName(String category);
}
