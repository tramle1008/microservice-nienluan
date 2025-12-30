package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.AddressDTO;
import com.ecommerce.auth.dto.ProvinceDTO;
import com.ecommerce.auth.dto.WardDTO;
import com.ecommerce.auth.exceptioons.ResourceNotFoundException;
import com.ecommerce.auth.exceptioons.UnauthorizedException;
import com.ecommerce.auth.models.*;
import com.ecommerce.auth.reponsitory.*;
import com.ecommerce.auth.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final ModelMapper modelMapper;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final ProvinceRepository provinceRepository;
    private final WardRepository wardRepository;
    private final AuthUtil authUtil;

    @Override
    public AddressDTO addAddress(Long userId, Long provinceId, Long wardId,
                                 String detail, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Province province = provinceRepository.findById(provinceId)
                .orElseThrow(() -> new ResourceNotFoundException("Province", "id", provinceId));

        Ward ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward", "id", wardId));

        // Kiểm tra ward có thuộc province không
        if (!ward.getProvince().getId().equals(provinceId)) {
            throw new IllegalArgumentException("Xã/Phường không thuộc tỉnh này");
        }

        Address address = new Address();
        address.setUser(user);
        address.setProvince(province);
        address.setWard(ward);
        address.setProvinceName(province.getName());
        address.setWardName(ward.getName());
        address.setDetail(detail);
        address.setPhoneNumber(phone);

        Address saved = addressRepository.save(address);
        return modelMapper.map(saved, AddressDTO.class);
    }

    @Override
    public AddressDTO updateAddress(Long addressId, Long provinceId, Long wardId,
                                    String detail, String phone) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        User currentUser = authUtil.getCurrentUserEntity();
        if (!address.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new UnauthorizedException("Không thể sửa địa chỉ này");
        }

        Province province = provinceRepository.findById(provinceId)
                .orElseThrow(() -> new ResourceNotFoundException("Province", "id", provinceId));

        Ward ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward", "id", wardId));

        if (!ward.getProvince().getId().equals(provinceId)) {
            throw new IllegalArgumentException("Xã/Phường không thuộc tỉnh này");
        }

        address.setProvince(province);
        address.setWard(ward);
        address.setProvinceName(province.getName());
        address.setWardName(ward.getName());
        address.setDetail(detail);
        address.setPhoneNumber(phone);

        Address saved = addressRepository.save(address);
        return modelMapper.map(saved, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getUserAddresses(Long userId) {
        return addressRepository.findByUser_UserId(userId)
                .stream()
                .map(a -> modelMapper.map(a, AddressDTO.class))
                .toList();
    }

    @Override
    public void deleteAddress(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        User currentUser = authUtil.getCurrentUserEntity();
        if (!address.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new UnauthorizedException("Không thể xóa địa chỉ này");
        }
        addressRepository.delete(address);
    }

    @Override
    public List<ProvinceDTO> getProvinces() {
        return provinceRepository.findAll()
                .stream()
                .map(p -> new ProvinceDTO(p.getId(), p.getName()))
                .toList();
    }

    @Override
    public List<WardDTO> getWardsByProvince(Long provinceId) {
        return wardRepository.findByProvince_Id(provinceId)
                .stream()
                .map(w -> new WardDTO(w.getId(), w.getName(), w.getProvince().getId()))
                .toList();
    }
}