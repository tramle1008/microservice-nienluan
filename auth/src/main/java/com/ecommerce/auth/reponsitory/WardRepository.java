package com.ecommerce.auth.reponsitory;

import com.ecommerce.auth.models.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WardRepository extends JpaRepository<Ward, Long> {
    List<Ward> findByProvince_Id(Long provinceId);
}