package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.*;
import com.ecommerce.auth.models.*;
import com.ecommerce.auth.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

//##### ADMIN############
    // Lấy tất cả địa chỉ của user
    @GetMapping("/user")
    public List<AddressDTO> getUserAddresses(@RequestHeader("X-User-Id") Long userId) {
        return addressService.getUserAddresses(userId);
    }

    // Thêm địa chỉ
    @PostMapping("/user")
    public AddressDTO addAddress(  @RequestHeader("X-User-Id") Long userId,
                                 @RequestBody AddressRequestDTO request) {
        return addressService.addAddress(
                userId,
                request.getProvinceId(),
                request.getWardId(),
                request.getDetail(),
                request.getPhoneNumber()
        );
    }
    // Xóa địa chỉ
    @DeleteMapping("/{addressId}")
    public void deleteAddress(@PathVariable Long addressId) {
        addressService.deleteAddress(addressId);
    }

//    thay doi dia chỉ
@PutMapping("/{addressId}")
public AddressDTO updateAddress(@PathVariable Long addressId,
                                @RequestBody AddressRequestDTO request) {
    return addressService.updateAddress(
            addressId,
            request.getProvinceId(),
            request.getWardId(),
            request.getDetail(),
            request.getPhoneNumber()
    );
}


//########## PUBLIC ###########
    // API lấy dữ liệu tỉnh/huyện/xã
    // API lấy tỉnh + xã
    @GetMapping("/provinces")
    public List<ProvinceDTO> getProvinces() {
        return addressService.getProvinces();
    }

    @GetMapping("/wards")
    public List<WardDTO> getWards(@RequestParam Long provinceId) {
        return addressService.getWardsByProvince(provinceId);
    }
}
