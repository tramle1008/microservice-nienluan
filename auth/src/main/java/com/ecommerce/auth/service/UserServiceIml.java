package com.ecommerce.auth.service;


import com.ecommerce.auth.dto.UserDTO;
import com.ecommerce.auth.exceptioons.ResourceNotFoundException;
import com.ecommerce.auth.models.AppRole;
import com.ecommerce.auth.models.User;
import com.ecommerce.auth.reponsitory.UserRepository;
import com.ecommerce.auth.security.request.UpdateUserRequest;
import com.ecommerce.auth.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceIml implements   UserService{
    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthUtil authUtil;


    @Autowired
    private PasswordEncoder encoder;

    @Override
    public List<UserDTO> getAllUser() {
        List<User> user = userRepository.findByRoles_RoleName(AppRole.ROLE_USER);
        List<UserDTO> userList = user.stream().map(
                u -> new UserDTO(
                        u.getUserId(),
                        u.getUserName(),
                        u.getEmail()
                )
        ).collect(Collectors.toList());
        return userList;
    }

    @Override
    public List<UserDTO> getAllAdmin() {
        List<User> admin = userRepository.findByRoles_RoleName(AppRole.ROLE_ADMIN);
        List<UserDTO> userList = admin.stream().map(
                a -> new UserDTO(
                        a.getUserId(),
                        a.getUserName(),
                        a.getEmail()
                )
        ).collect(Collectors.toList());
        return userList;
    }

    @Transactional
    @Override
    public String updateCurrentUser(UpdateUserRequest request) {
        Long currentId = authUtil.getCurrentUserId(); // hoặc lấy từ SecurityContext

        User user = userRepository.findById(currentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentId));

        // Check nếu đổi sang email đã tồn tại
        if (request.getEmail() != null &&
                !request.getEmail().equalsIgnoreCase(user.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        // Check nếu đổi sang username đã tồn tại
        if (request.getUsername() != null &&
                !request.getUsername().equals(user.getUserName()) &&
                userRepository.existsByUserName(request.getUsername())) {
            throw new RuntimeException("Username đã tồn tại");
        }

        // Cập nhật thông tin
        if (request.getUsername() != null) user.setUserName(request.getUsername());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(encoder.encode(request.getPassword()));
        }

        userRepository.save(user);
        return "Cập nhật thông tin người dùng thành công.";
    }

}
