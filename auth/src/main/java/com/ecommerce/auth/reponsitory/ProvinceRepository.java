package com.ecommerce.auth.reponsitory;

import com.ecommerce.auth.models.Province;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Long> {
    Page<Province> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
