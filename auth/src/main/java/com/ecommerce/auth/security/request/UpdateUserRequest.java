package com.ecommerce.auth.security.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String username;

    @Email
    private String email;

    private String password; // có thể để trống nếu không muốn đổi
}
