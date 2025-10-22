package com.ecommerce.user.controller;


import com.ecommerce.user.dto.MessageResponse;
import com.ecommerce.user.dto.SignupRequest;
import com.ecommerce.user.dto.UserDTO;

import com.ecommerce.user.models.AppRole;
import com.ecommerce.user.models.Role;
import com.ecommerce.user.models.User;
import com.ecommerce.user.repository.RoleRepository;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    RoleRepository roleRepository;

    //  Lấy danh sách tất cả ROLE User
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUser();
        return ResponseEntity.ok(users);
    }
    //Lấy danh sách tất cả ROLE
    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> getAllUsersRole() {
        List<UserDTO> userDTOS = userService.getAll();
        return ResponseEntity.ok(userDTOS);
    }

    //Lấy user theo id
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        UserDTO user = userService.getUser(id);
        return new  ResponseEntity<>(user, HttpStatus.OK);
    }

//dang ky
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody @Valid SignupRequest signupRequest) {

//        // Kiểm tra user name đã tồn tại
//        if (userRepository.existsByUserName(signupRequest.getUsername())) {
//            return ResponseEntity
//                    .badRequest()
//                    .body(new MessageResponse("Error: Tên đăng ký người dùng đã tồn tại"));
//        }

        // Kiểm tra email đã tồn tại (sửa lỗi dùng sai getUserName())
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Lỗi: Email đăng ký đã tồn tại"));
        }

        // Tạo user mới với mật khẩu đã mã hóa
        User user = new User(
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                encoder.encode(signupRequest.getPassword())
        );

        Set<String> strRole = signupRequest.getRole();
        Set<Role> roles = new HashSet<>();

        // Nếu không gửi role => mặc định là ROLE_USER
        if (strRole == null || strRole.isEmpty()) {
            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseThrow(() -> new IllegalArgumentException("Lỗi: Role USER không tìm thấy"));
            roles.add(userRole);
        } else {
            for (String roleStr : strRole) {
                switch (roleStr.toLowerCase()) {
                    case "admin":
                        Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                                .orElseThrow(() -> new IllegalArgumentException("Error: Role ADMIN không tìm thấy"));
                        roles.add(adminRole);
                        break;
                    case "deliver":
                        Role deliverRole = roleRepository.findByRoleName(AppRole.ROLE_DELIVER)
                                .orElseThrow(() -> new IllegalArgumentException("Error: Role ADMIN không tìm thấy"));
                        roles.add(deliverRole);
                        break;
                    default:
                        Role defaultRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                                .orElseThrow(() -> new IllegalArgumentException("Error: Role USER không tìm thấy"));
                        roles.add(defaultRole);
                }
            }
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Đăng ký thành công"));
    }


    // Xóa User theo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
       String result=  userService.deleteUser(id);
       return new ResponseEntity<>(result, HttpStatus.OK);
    }


}
