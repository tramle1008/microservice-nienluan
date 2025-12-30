package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.DashboardDTO;
import com.ecommerce.auth.dto.UserDTO;
import com.ecommerce.auth.dto.UserProfileDTO;
import com.ecommerce.auth.models.AppRole;
import com.ecommerce.auth.reponsitory.UserRepository;
import com.ecommerce.auth.service.AdminService;
import com.ecommerce.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Secured("ROLE_ADMIN")
public class AdminController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUser());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserProfileDTO> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserProfile(id));
    }
    @GetMapping("/count")
    public Long getUserCount() {
        return  userRepository.findByRoles_RoleName(AppRole.ROLE_USER).stream().count();
    }


    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> getDashboard() {
        return ResponseEntity.ok(adminService.getStats());
    }
}
