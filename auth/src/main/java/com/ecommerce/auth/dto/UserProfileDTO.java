package com.ecommerce.auth.dto;
//toàn bộ thông tin về username email + address

import lombok.*;

import java.util.List;

// UserProfileDTO.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Long userId;
    private String username;
    private String email;
    private List<AddressDTO> addresses;
}