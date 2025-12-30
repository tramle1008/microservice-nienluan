package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.UserDTO;
import com.ecommerce.auth.dto.UserProfileDTO;
import com.ecommerce.auth.models.User;
import com.ecommerce.auth.security.jwt.JwtUtils;
import com.ecommerce.auth.security.request.UpdateUserRequest;
import com.ecommerce.auth.security.response.UserInfoResponse;
import com.ecommerce.auth.security.services.UserDetailsImpl;
import com.ecommerce.auth.service.UserService;
import com.ecommerce.auth.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    JwtUtils jwtUtils;

//    auth
    @GetMapping("/admin/allUsers")
    public ResponseEntity<List<UserDTO>> getUser(){
        List<UserDTO> allUser= userService.getAllUser();
        return new ResponseEntity<>(allUser, HttpStatus.OK);
    }
    @GetMapping("/admin")
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

    @GetMapping("/user")
    public ResponseEntity<?> getUserDetail(Authentication authentication, HttpServletRequest request){
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bạn chưa đăng ký");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());
        String jwtToken = jwtUtils.getJWTFromHeader(request);

        UserInfoResponse response = new UserInfoResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles,
                jwtToken
        );
        return  ResponseEntity.ok().body(response);
    }
}
