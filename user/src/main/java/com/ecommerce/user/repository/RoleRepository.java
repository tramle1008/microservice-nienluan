package com.ecommerce.user.repository;

import com.ecommerce.user.models.AppRole;
import com.ecommerce.user.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(AppRole appRole);

}
