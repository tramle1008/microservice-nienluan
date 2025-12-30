package com.ecommerce.auth.reponsitory;

import com.ecommerce.auth.models.Address;
import com.ecommerce.auth.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser_UserId(Long userId);
}
