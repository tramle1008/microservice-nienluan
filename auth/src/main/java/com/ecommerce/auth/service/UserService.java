package com.ecommerce.auth.service;



import com.ecommerce.auth.dto.UserDTO;
import com.ecommerce.auth.security.request.UpdateUserRequest;

import java.util.List;

public interface UserService {
    List<UserDTO> getAllUser();

    List<UserDTO> getAllAdmin();

    String updateCurrentUser(UpdateUserRequest request);
}
