package com.ecommerce.product.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {
    //creat method uploadImage
    String uploadImage(String path, MultipartFile file) throws IOException;

    String getFullImageUrl(String imagePath);

    void deleteImage(String imagePath);
}
