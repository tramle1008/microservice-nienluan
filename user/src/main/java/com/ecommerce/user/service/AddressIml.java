package com.ecommerce.user.service;


import com.ecommerce.user.dto.AddressDTO;
import com.ecommerce.user.exceptions.ResourceNotFoundException;

import com.ecommerce.user.models.Address;
import com.ecommerce.user.models.User;
import com.ecommerce.user.repository.AddressRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressIml implements AddressService {
    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AddressRepository addressRepository;


    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {
        Address address = modelMapper.map(addressDTO, Address.class);
        List<Address> addressList = user.getAddressList();
        addressList.add(address);
        user.setAddressList(addressList);

        address.setUser(user);
        Address saveAddress = addressRepository.save(address);
        return modelMapper.map(saveAddress, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAdresses() {
        List<Address> addressList = addressRepository.findAll();
        List<AddressDTO> addressDTOLisst = addressList.stream().map(address -> modelMapper.map(address, AddressDTO.class))
                .collect(Collectors.toList());

        return addressDTOLisst;
    }

    @Override
    public List<AddressDTO> getUserAddresses(User user) {
        List<Address> addressList = addressRepository.findByUser(user);
        List<AddressDTO> addressDTOList = addressList.stream().map(a -> modelMapper.map(a, AddressDTO.class))
                .collect(Collectors.toList());
        return addressDTOList;
    }

    @Override
    public AddressDTO updateUserAddress(AddressDTO addressDTO, Long addressId) {

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("address", "address", addressId));


        // Cập nhật nếu có dữ liệu mới
        if (addressDTO.getDistrict() != null) {
            address.setDistrict(addressDTO.getDistrict());
        }

        if (addressDTO.getProvince() != null) {
            address.setProvince(addressDTO.getProvince());
        }

        if (addressDTO.getWard() != null) {
            address.setWard(addressDTO.getWard());
        }


        if (addressDTO.getPhoneNumber() != null) {
            address.setPhoneNumber(addressDTO.getPhoneNumber());
        }



        // 3. Lưu lại
        Address saveAddress = addressRepository.save(address);

        // 4. Trả về kết quả
        return modelMapper.map(saveAddress, AddressDTO.class);
    }

    @Override
    public AddressDTO deleteUserAddress(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Địa chỉ", "Địa chỉ", addressId));
        addressRepository.delete(address);
        return modelMapper.map(address, AddressDTO.class);
    }


}
