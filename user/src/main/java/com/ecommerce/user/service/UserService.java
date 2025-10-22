package com.ecommerce.user.service;



import com.ecommerce.user.dto.UserDTO;

import java.util.List;

public interface UserService {
    List<UserDTO> getAllUser();

    List<UserDTO> getAll();

    UserDTO getUser(Long id);

 String deleteUser(Long id);
}
