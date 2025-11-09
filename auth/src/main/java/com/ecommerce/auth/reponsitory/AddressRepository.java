package com.ecommerce.auth.reponsitory;

import com.ecommerce.auth.models.Address;
import com.ecommerce.auth.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser(User user);

}
