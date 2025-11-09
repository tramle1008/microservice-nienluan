package com.ecommerce.auth.reponsitory;


import com.ecommerce.auth.models.AppRole;
import com.ecommerce.auth.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(AppRole appRole);

}
