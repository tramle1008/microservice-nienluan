package com.ecommerce.auth.security.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {

    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    @NotBlank
    private String email;


    private Set<String> role;

    @NotBlank
    @Size(min = 8, max = 40, message = "Mật khẩu phải từ 8 đến 40 ký tự")
    private String password;

}
