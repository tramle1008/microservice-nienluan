package com.ecommerce.user.repository;



import com.ecommerce.user.models.Address;
import com.ecommerce.user.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser(User user);

}
