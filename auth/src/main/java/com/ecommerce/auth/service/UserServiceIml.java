package com.ecommerce.auth.service;


import com.ecommerce.auth.dto.AddressDTO;
import com.ecommerce.auth.dto.UserDTO;
import com.ecommerce.auth.dto.UserProfileDTO;
import com.ecommerce.auth.exceptioons.ResourceNotFoundException;
import com.ecommerce.auth.models.Address;
import com.ecommerce.auth.models.AppRole;
import com.ecommerce.auth.models.User;
import com.ecommerce.auth.reponsitory.AddressRepository;
import com.ecommerce.auth.reponsitory.UserRepository;
import com.ecommerce.auth.security.request.UpdateUserRequest;
import com.ecommerce.auth.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceIml implements   UserService{

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final AuthUtil authUtil;
    private final PasswordEncoder encoder;

    @Override
    public UserProfileDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<AddressDTO> addresses = addressRepository.findByUser_UserId(userId)
                .stream()
                .map(this::mapToAddressDTO)
                .toList();

        UserProfileDTO profile = new UserProfileDTO();
        profile.setUserId(user.getUserId());
        profile.setUsername(user.getUserName());
        profile.setEmail(user.getEmail());
        profile.setAddresses(addresses);
        return profile;
    }


    private AddressDTO mapToAddressDTO(Address address) {
        AddressDTO dto = new AddressDTO();
        dto.setAddressId(address.getAddressId());
        dto.setProvinceId(address.getProvince().getId());
        dto.setProvince(address.getProvinceName());
        dto.setWard(address.getWardName());
        dto.setDetail(address.getDetail());
        dto.setPhoneNumber(address.getPhoneNumber());
        return dto;
    }

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
    public List<UserDTO> getAllAdmin() {
        List<User> admin = userRepository.findByRoles_RoleName(AppRole.ROLE_ADMIN);
        List<UserDTO> userList = admin.stream().map(
                a -> new UserDTO(
                        a.getUserId(),
                        a.getUserName(),
                        a.getEmail()
                )
        ).collect(Collectors.toList());
        return userList;
    }

    @Transactional
    @Override
    public String updateCurrentUser(UpdateUserRequest request) {
        Long currentId = authUtil.getCurrentUserId(); // hoặc lấy từ SecurityContext

        User user = userRepository.findById(currentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentId));

        // Check nếu đổi sang email đã tồn tại
        if (request.getEmail() != null &&
                !request.getEmail().equalsIgnoreCase(user.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        // Check nếu đổi sang username đã tồn tại
        if (request.getUsername() != null &&
                !request.getUsername().equals(user.getUserName()) &&
                userRepository.existsByUserName(request.getUsername())) {
            throw new RuntimeException("Username đã tồn tại");
        }

        // Cập nhật thông tin
        if (request.getUsername() != null) user.setUserName(request.getUsername());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(encoder.encode(request.getPassword()));
        }

        userRepository.save(user);
        return "Cập nhật thông tin người dùng thành công.";
    }

}
