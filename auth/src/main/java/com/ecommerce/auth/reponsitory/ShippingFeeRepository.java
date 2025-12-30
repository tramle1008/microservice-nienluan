package com.ecommerce.auth.reponsitory;

import com.ecommerce.auth.models.ShippingFeeByProvince;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShippingFeeRepository extends JpaRepository<ShippingFeeByProvince, Long> {
}