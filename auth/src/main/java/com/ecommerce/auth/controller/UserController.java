package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.UserDTO;
import com.ecommerce.auth.models.User;
import com.ecommerce.auth.security.jwt.JwtUtils;
import com.ecommerce.auth.security.request.UpdateUserRequest;
import com.ecommerce.auth.security.response.UserInfoResponse;
import com.ecommerce.auth.security.services.UserDetailsImpl;
import com.ecommerce.auth.service.UserService;
import com.ecommerce.auth.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    JwtUtils jwtUtils;
    @GetMapping("/public/user")
    public ResponseEntity<List<UserDTO>> getUser(){
        List<UserDTO> allUser= userService.getAllUser();
        return new ResponseEntity<>(allUser, HttpStatus.OK);
    }
    @GetMapping("/public/admin")
    public ResponseEntity<List<UserDTO>> getAdmin(){
        List<UserDTO> allAdmin= userService.getAllAdmin();
        return new ResponseEntity<>(allAdmin, HttpStatus.OK);
    }
    @PutMapping("/update/user")
    public ResponseEntity<?> updateUser(@RequestBody UpdateUserRequest request) {
        String result = userService.updateCurrentUser(request);

        User updatedUser = authUtil.getCurrentUserEntity();
        UserDetailsImpl userDetails = UserDetailsImpl.build(updatedUser);
        String newJwtToken = jwtUtils.generateToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList();

        return ResponseEntity.ok(new UserInfoResponse(
                updatedUser.getUserId(),
                updatedUser.getUserName(),
                updatedUser.getEmail(),
                roles,
                newJwtToken
        ));
    }

}
