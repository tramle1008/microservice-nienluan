package com.ecommerce.user.service;


import com.ecommerce.user.dto.UserDTO;

import com.ecommerce.user.models.AppRole;
import com.ecommerce.user.models.User;
import com.ecommerce.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceIml implements UserService {
    @Autowired
    UserRepository userRepository;


    private final PasswordEncoder encoder = new BCryptPasswordEncoder();


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
    public List<UserDTO> getAll() {
        List<User> userRepositoryAll = userRepository.findAll();
        List<UserDTO> userList = userRepositoryAll.stream().map(
                a -> new UserDTO(
                        a.getUserId(),
                        a.getUserName(),
                        a.getEmail()
                )
        ).collect(Collectors.toList());
        return userList;
    }

    @Override
    public UserDTO getUser(Long id) {
        return userRepository.findById(id)
                .map(user -> new UserDTO(
                        user.getUserId(),
                        user.getUserName(),
                        user.getEmail()
                ))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không thể tìm được User với id: " + id));
    }
    @Override
    public String deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tồn tại User với id: " + id));

        userRepository.delete(user);
        return "Xóa User thành công với id: " + id;
    }

}
