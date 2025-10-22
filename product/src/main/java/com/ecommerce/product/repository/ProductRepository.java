package com.ecommerce.product.repository;


import com.ecommerce.product.models.Category;
import com.ecommerce.product.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface ProductRepository extends JpaRepository<Product, Long> , JpaSpecificationExecutor<Product> {
  Page<Product> findByCategory(Category category, Pageable pageable);
    Page<Product> findByProductNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String productName,
            String description,
            Pageable pageable
    );
}
