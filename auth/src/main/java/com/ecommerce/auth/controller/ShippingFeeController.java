package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.AddressDTO;
import com.ecommerce.auth.dto.ShippingFeeDTO;
import com.ecommerce.auth.dto.ShippingFeeResponse;
import com.ecommerce.auth.dto.UpdateFeeShipRequest;
import com.ecommerce.auth.exceptioons.ResourceNotFoundException;
import com.ecommerce.auth.models.Address;
import com.ecommerce.auth.models.Province;
import com.ecommerce.auth.models.ShippingFeeByProvince;
import com.ecommerce.auth.reponsitory.AddressRepository;
import com.ecommerce.auth.reponsitory.ProvinceRepository;
import com.ecommerce.auth.reponsitory.ShippingFeeRepository;
import com.ecommerce.auth.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
public class ShippingFeeController {

    private final ShippingFeeRepository shippingFeeRepository;
    private final AddressRepository addressRepository;
    private final ProvinceRepository provinceRepository;

    //auth
    @GetMapping("/fee/province/{provinceId}")
    public ResponseEntity<ShippingFeeResponse> getFee(@PathVariable Long provinceId) {
        ShippingFeeByProvince fee = shippingFeeRepository.findById(provinceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phí ship cho tỉnh này"));

        ShippingFeeResponse resp = new ShippingFeeResponse();
        resp.setProvinceId(provinceId);
        resp.setDefaultFee(fee.getDefaultFee());
        resp.setFreeShippingThreshold(fee.getFreeShippingThreshold());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/fee/address/{addressId}")
    public ResponseEntity<ShippingFeeResponse> getFeeByAddress(@PathVariable Long addressId) {

        // 1. LẤY ADDRESS THEO ID
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ"));

        // 2. LẤY PROVINCE ID
        Long provinceId = address.getProvince().getId();

        // 3. TÌM SHIPPING FEE THEO PROVINCE
        ShippingFeeByProvince fee = shippingFeeRepository.findById(provinceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phí ship cho tỉnh này"));

        // 4. TRẢ VỀ DTO
        ShippingFeeResponse resp = new ShippingFeeResponse();
        resp.setProvinceId(provinceId);
        resp.setDefaultFee(fee.getDefaultFee());
        resp.setProvince(address.getProvinceName());
        resp.setWard(address.getWardName());
        resp.setDetail(address.getDetail());
        resp.setPhoneNumber(address.getPhoneNumber());
        resp.setFreeShippingThreshold(fee.getFreeShippingThreshold());

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/fee/all")
    public ResponseEntity<Page<ShippingFeeDTO>> getAllFees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String sortBy,     // BỎ defaultValue có dấu chấm
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search
    ) {
        // === 1. Chỉ cho phép sort theo các field hợp lệ của Province ===
        String safeSortBy = "name"; // mặc định sort theo tên tỉnh
        if (sortBy != null && !sortBy.isBlank()) {
            // Chỉ chấp nhận "id" hoặc "name" (tránh lỗi PropertyReferenceException)
            if ("id".equalsIgnoreCase(sortBy.trim())) {
                safeSortBy = "id";
            } else if ("name".equalsIgnoreCase(sortBy.trim())) {
                safeSortBy = "name";
            }
            // Nếu muốn sort theo phí → để frontend sort, hoặc làm riêng sau
        }

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, safeSortBy));

        // === 2. Lấy dữ liệu có phân trang + tìm kiếm ===
        Page<Province> provincePage;
        if (search != null && !search.trim().isEmpty()) {
            provincePage = provinceRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        } else {
            provincePage = provinceRepository.findAll(pageable);
        }

        // === 3. Map sang DTO + tự động tạo phí nếu chưa có ===
        Page<ShippingFeeDTO> dtoPage = provincePage.map(province -> {
            ShippingFeeByProvince fee = province.getShippingFee();

            if (fee == null) {
                fee = new ShippingFeeByProvince();
                fee.setProvinceId(province.getId());
                fee.setProvince(province);
                fee.setDefaultFee(BigDecimal.valueOf(50000));
                fee.setFreeShippingThreshold(BigDecimal.valueOf(500000));
                fee = shippingFeeRepository.save(fee);
                province.setShippingFee(fee); // giữ quan hệ sạch
            }

            return new ShippingFeeDTO(
                    province.getId(),
                    province.getName(),
                    fee.getDefaultFee(),
                    null, // wardName
                    fee.getDefaultFee(),
                    fee.getFreeShippingThreshold()
            );
        });

        return ResponseEntity.ok(dtoPage);
    }

    @PutMapping("/fee/{provinceId}")
    public ResponseEntity<Void> updateByProvinceId(
            @PathVariable Long provinceId,
            @RequestBody UpdateFeeShipRequest request) {

        Province province = provinceRepository.findById(provinceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tỉnh với ID: " + provinceId));

        ShippingFeeByProvince fee = province.getShippingFee();

        // Nếu chưa có thì tạo mới với giá trị mặc định
        if (fee == null) {
            fee = new ShippingFeeByProvince();
            fee.setProvinceId(provinceId);
            fee.setProvince(province);
            fee.setDefaultFee(BigDecimal.valueOf(50000));
            fee.setFreeShippingThreshold(BigDecimal.valueOf(500000));
        }

        // Chỉ cập nhật những field được gửi lên (khác null)
        if (request.getDefaultFee() != null) {
            fee.setDefaultFee(request.getDefaultFee());
        }
        if (request.getFreeShippingThreshold() != null) {
            fee.setFreeShippingThreshold(request.getFreeShippingThreshold());
        }

        shippingFeeRepository.save(fee);

        // 204 No Content - Cập nhật thành công, không trả về body
        return ResponseEntity.noContent().build();
    }
}
