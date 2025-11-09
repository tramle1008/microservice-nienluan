package com.ecommerce.auth.dto;

import lombok.Data;

@Data
public class UserInfo {
    private String id;
    private String username;
    private String role;

    public UserInfo(Long userId, String username, String role) {
        this.id = String.valueOf(userId);
        this.username = username;
        this.role = role;
    }
}

