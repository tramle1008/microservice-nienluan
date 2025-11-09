package com.ecommerce.product.repository;


import com.ecommerce.product.models.Category;
import com.ecommerce.product.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface ProductRepository extends JpaRepository<Product, Long> , JpaSpecificationExecutor<Product> {
  Page<Product> findByCategory(Category category, Pageable pageable);

  @Query("SELECT p FROM Product p JOIN FETCH p.variants WHERE p.category.categoryId = :categoryId")
  List<Product> findByCategoryIdWithVariants(@Param("categoryId") Long categoryId);

  @Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants WHERE p.productId = :id")
  Optional<Product> findByIdWithVariants(@Param("id") Long id);

  @Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants WHERE p.category.categoryId = :categoryId")
  Page<Product> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

  @Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants " +
          "WHERE (:key IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :key, '%'))) " +
          "AND (:categoryId IS NULL OR p.category.categoryId = :categoryId)")
  Page<Product> findAllWithFilters(
          @Param("key") String key,
          @Param("categoryId") Long categoryId,
          Pageable pageable);

  @Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants " +
          "WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
          "OR LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);


  Page<Product> findByCategoryCategoryId(Long categoryId, Pageable pageable);


  @Query(value = "SELECT * FROM product WHERE category_id IN :categoryIds ORDER BY RAND()",
          countQuery = "SELECT COUNT(*) FROM product WHERE category_id IN :categoryIds",
          nativeQuery = true)
  Page<Product> findRandomInCategoryIds(
          @Param("categoryIds") List<Long> categoryIds,
          Pageable pageable
  );


    @Query("SELECT DISTINCT p FROM Product p " +
            "JOIN p.productDiscounts pd " +
            "JOIN pd.discount d " +
            "WHERE d.active = true " +  // hoặc check ngày bắt đầu/kết thúc nếu có
            "ORDER BY p.productId ASC")
    List<Product> findDiscountedProducts();

   @EntityGraph(attributePaths = {"variants"})
   Page<Product> findByCategory_CategoryIdIn(List<Long> categoryIds, Pageable pageable);


}
