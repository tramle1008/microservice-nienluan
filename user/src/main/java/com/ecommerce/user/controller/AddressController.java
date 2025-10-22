package com.ecommerce.user.controller;


import com.ecommerce.user.dto.AddressDTO;

import com.ecommerce.user.models.User;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AddressController {

    @Autowired
    AddressService addressService;

    @Autowired
    UserRepository userRepository;


    @PostMapping("/auth/user/{id}/addresses")
    public ResponseEntity<AddressDTO> createAddress(
            @Valid @RequestBody AddressDTO addressDTO,
            @PathVariable Long id) {
        // Tìm user theo id
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm được User với id : " + id));
        // Gọi service để lưu address kèm user
        AddressDTO savedAddress = addressService.createAddress(addressDTO, user);

        return new ResponseEntity<>(savedAddress, HttpStatus.CREATED);
    }


    @GetMapping("/admin/user/addresses")
    public ResponseEntity<List<AddressDTO>> getAllAddresses(){
       List<AddressDTO> addressDTOList = addressService.getAdresses();
          return new ResponseEntity<>(addressDTOList, HttpStatus.OK);
    }

//    get address user until
    @GetMapping("/auth/user/{id}/addresses")
    public ResponseEntity<List<AddressDTO>> getUserAddresses(@PathVariable Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tim được User với id: " + id));
        List<AddressDTO> addressDTOList = addressService.getUserAddresses(user);
        return new ResponseEntity<>(addressDTOList, HttpStatus.OK);
    }

    @PutMapping("/auth/user/addresses/update/{addressId}")
    public ResponseEntity<AddressDTO> updateAddress(@Valid @RequestBody AddressDTO addressDTO,
                                                      @PathVariable("addressId") Long addressId) {
        AddressDTO updatedUserAddressDTO = addressService.updateUserAddress(addressDTO, addressId);
        return new ResponseEntity<>(updatedUserAddressDTO, HttpStatus.OK);
    }

    @DeleteMapping("/auth/user/address/delete/{addressId}")
    public ResponseEntity<String> deleteAddress(@PathVariable("addressId") Long addressId){
        addressService.deleteUserAddress(addressId);
        return new ResponseEntity<>("successfully ", HttpStatus.OK);
    }


}
