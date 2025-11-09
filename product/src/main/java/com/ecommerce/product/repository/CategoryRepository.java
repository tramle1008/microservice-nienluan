package com.ecommerce.product.repository;

//Crud

import com.ecommerce.product.models.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {


    @Query("SELECT c FROM categories c WHERE c.parent IS NULL")
    List<Category> findRootCategories();

    Category findByCategoryName(String category);

    @Query("SELECT c FROM categories c WHERE c.categoryName = :name AND c.parent = :parent")
    Category findByCategoryNameAndParent(@Param("name") String name, @Param("parent") Category parent);

    @Query("SELECT c FROM categories c WHERE c.categoryName = :name AND c.parent IS NULL")
    Optional<Category> findRootByName(@Param("name") String name);

    @Query("SELECT c FROM categories c WHERE c.parent IS NOT NULL")
    Page<Category> findChildCategories(Pageable pageable);

}
