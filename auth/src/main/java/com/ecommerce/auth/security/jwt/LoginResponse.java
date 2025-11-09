package com.ecommerce.auth.security.jwt;

import java.util.List;

public class LoginResponse {
    private String jwtToken;
    private String userName;
    private String email;
    private List<String> role;

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public List<String> getRole() {
        return role;
    }

    public void setRole(List<String> role) {
        this.role = role;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LoginResponse(String jwtToken, List<String> role, String userName) {
        this.jwtToken = jwtToken;
        this.role = role;
        this.userName = userName;
    }
    public LoginResponse( String userName, String email, List<String> role, String jwtToken) {
        this.userName = userName;
        this.email = email;
        this.role = role;
        this.jwtToken = jwtToken;
    }
}
